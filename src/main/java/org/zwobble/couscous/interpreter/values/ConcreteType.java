package org.zwobble.couscous.interpreter.values;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.PositionalArguments;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.Executor;
import org.zwobble.couscous.interpreter.NoSuchMethod;
import org.zwobble.couscous.interpreter.UnexpectedValueType;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;
import org.zwobble.couscous.util.Casts;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static java.util.stream.Collectors.toMap;

import lombok.val;

public class ConcreteType {
    public static class Builder<T> {
        private final ImmutableMap.Builder<String, MethodValue> methods =
            ImmutableMap.builder();
        private final ImmutableMap.Builder<String, StaticMethodValue> staticMethods =
            ImmutableMap.builder();
        private final Class<T> interpreterValueType;
        private final TypeName name;
        
        public Builder(Class<T> interpreterValueType, TypeName name) {
            this.interpreterValueType = interpreterValueType;
            this.name = name;
        }
        
        public Builder<T> method(
                String name,
                List<TypeName> argumentsTypes,
                BiFunction<Environment, MethodCallArguments<T>, InterpreterValue> method) {
            methods.put(name, new MethodValue(argumentsTypes, (environment, arguments) -> {
                return Casts.tryCast(interpreterValueType, arguments.getReceiver())
                    .map(typedReceiver ->
                        method.apply(environment, MethodCallArguments.of(typedReceiver, arguments.getPositionalArguments())))
                    .orElseThrow(() -> new RuntimeException("receiver is of wrong type"));
            }));
            return this;
        }
        
        public Builder<T> staticMethod(
                String name,
                List<TypeName> argumentsTypes,
                BiFunction<Environment, PositionalArguments, InterpreterValue> method) {
            staticMethods.put(name, new StaticMethodValue(argumentsTypes, method));
            return this;
        }
        
        public ConcreteType build() {
            return new ConcreteType(
                name,
                new MethodValue(
                    ImmutableList.of(),
                    (environment, arguments) -> InterpreterValues.UNIT),
                methods.build(),
                staticMethods.build());
        }
    }
    
    public static ConcreteType fromNode(ClassNode classNode) {
        val constructor = new MethodValue(
            getArgumentTypes(classNode.getConstructor().getArguments()),
            (environment, arguments) -> Executor.callMethod(
                environment,
                classNode.getConstructor(),
                Optional.of(arguments.getReceiver()),
                arguments.getPositionalArguments()));
        
        val methods = classNode.getMethods().stream()
            .filter(method -> !method.isStatic())
            .collect(toMap(
                method -> method.getName(),
                method -> {
                    val argumentTypes = getArgumentTypes(method.getArguments());
                    
                    return new MethodValue(argumentTypes, (environment, arguments) -> {
                        return Executor.callMethod(
                            environment,
                            method,
                            Optional.of(arguments.getReceiver()),
                            arguments.getPositionalArguments());
                    });
                }));
        
        val staticMethods = classNode.getMethods()
            .stream()
            .filter(method -> method.isStatic())
            .collect(toMap(
                method -> method.getName(),
                method -> {
                    val argumentTypes = getArgumentTypes(method.getArguments());
                    
                    return new StaticMethodValue(argumentTypes, (environment, arguments) -> {
                        return Executor.callMethod(
                            environment,
                            method,
                            Optional.empty(),
                            arguments);
                    });
                }));
        return new ConcreteType(
            classNode.getName(),
            constructor,
            methods,
            staticMethods);
    }
    
    private static List<TypeName> getArgumentTypes(List<FormalArgumentNode> arguments) {
        return arguments
            .stream()
            .map(arg -> arg.getType())
            .collect(Collectors.toList());
    }

    private final TypeName name;
    private final MethodValue constructor;
    private final Map<String, MethodValue> methods;
    private final Map<String, StaticMethodValue> staticMethods;

    public ConcreteType(
            TypeName name,
            MethodValue constructor,
            Map<String, MethodValue> methods,
            Map<String, StaticMethodValue> staticMethods) {
        this.name = name;
        this.constructor = constructor;
        this.methods = methods;
        this.staticMethods = staticMethods;
    }
    
    public TypeName getName() {
        return name;
    }

    public static <T> ConcreteType.Builder<T> builder(Class<T> interpreterValueType, TypeName reference) {
        return new Builder<>(interpreterValueType, reference);
    }

    public static <T> ConcreteType.Builder<T> builder(Class<T> interpreterValueType, String name) {
        return builder(interpreterValueType, TypeName.of(name));
    }

    public InterpreterValue callMethod(Environment environment, InterpreterValue receiver, String methodName, List<InterpreterValue> arguments) {
        val method = findMethod(methods, methodName, arguments);
        return method.apply(
            environment,
            MethodCallArguments.of(receiver, new PositionalArguments(arguments)));
    }

    public InterpreterValue callStaticMethod(Environment environment, String methodName, List<InterpreterValue> arguments) {
        val method = findMethod(staticMethods, methodName, arguments);
        return method.apply(environment, new PositionalArguments(arguments));
    }

    public InterpreterValue callConstructor(
            Environment environment,
            List<InterpreterValue> arguments) {
        val object = new ObjectInterpreterValue(this);
        constructor.apply(
            environment,
            MethodCallArguments.of(object, new PositionalArguments(arguments)));
        return object;
    }
    
    private static <T extends Callable> T findMethod(Map<String, T> methods, String methodName, List<InterpreterValue> arguments) {
        if (!methods.containsKey(methodName)) {
            throw new NoSuchMethod(methodName);
        }
        val method = methods.get(methodName);
        if (method.getArgumentTypes().size() != arguments.size()) {
            throw new WrongNumberOfArguments(method.getArgumentTypes().size(), arguments.size());
        }
        
        for (int index = 0; index < arguments.size(); index++) {
            val formalArgumentType = method.getArgumentTypes().get(index);
            val actualArgumentType = arguments.get(index).getType();
            if (!formalArgumentType.equals(actualArgumentType.getName())) {
                throw new UnexpectedValueType(formalArgumentType, actualArgumentType.getName());
            }
        }
        return method;
    }
    
    @Override
    public String toString() {
        return "ConcreteType<" + name + ">";
    }
}
