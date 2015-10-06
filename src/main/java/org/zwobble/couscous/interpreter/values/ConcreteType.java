package org.zwobble.couscous.interpreter.values;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.Executor;
import org.zwobble.couscous.interpreter.NoSuchMethod;
import org.zwobble.couscous.interpreter.UnexpectedValueType;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;

import com.google.common.collect.ImmutableMap;

import static java.util.stream.Collectors.toMap;

import lombok.val;

public class ConcreteType<T> {
    public static class Builder<T> {
        private final ImmutableMap.Builder<String, MethodValue<T>> methods =
            ImmutableMap.builder();
        private final ImmutableMap.Builder<String, StaticMethodValue> staticMethods =
            ImmutableMap.builder();
        private final TypeName name;
        
        public Builder(TypeName name) {
            this.name = name;
        }
        
        public Builder<T> method(
                String name,
                List<TypeName> argumentsTypes,
                BiFunction<T, Arguments, InterpreterValue> method) {
            methods.put(name, new MethodValue<T>(argumentsTypes, method));
            return this;
        }
        
        public Builder<T> staticMethod(
                String name,
                List<TypeName> argumentsTypes,
                BiFunction<Environment, Arguments, InterpreterValue> method) {
            staticMethods.put(name, new StaticMethodValue(argumentsTypes, method));
            return this;
        }
        
        public ConcreteType<T> build() {
            return new ConcreteType<T>(name, methods.build(), staticMethods.build());
        }
    }
    
    public static ConcreteType<?> fromNode(ClassNode classNode) {
        val staticMethods = classNode.getMethods()
            .stream()
            .filter(method -> method.isStatic())
            .collect(toMap(
                method -> method.getName(),
                method -> {
                    List<TypeName> argumentTypes = method.getArguments()
                        .stream()
                        .map(arg -> arg.getType())
                        .collect(Collectors.toList());
                    return new StaticMethodValue(argumentTypes, (environment, arguments) -> {
                        return Executor.callMethod(environment, method, arguments);
                    });
                }));
        return new ConcreteType<Void>(
            classNode.getName(),
            ImmutableMap.of(),
            staticMethods);
    }

    private TypeName name;
    private Map<String, MethodValue<T>> methods;
    private Map<String, StaticMethodValue> staticMethods;

    public ConcreteType(
            TypeName name,
            Map<String, MethodValue<T>> methods,
            Map<String, StaticMethodValue> staticMethods) {
        this.name = name;
        this.methods = methods;
        this.staticMethods = staticMethods;
    }
    
    public TypeName getName() {
        return name;
    }

    public static <T> ConcreteType.Builder<T> builder(TypeName reference) {
        return new Builder<>(reference);
    }

    public static <T> ConcreteType.Builder<T> builder(String name) {
        return builder(TypeName.of(name));
    }

    @SuppressWarnings("unchecked")
    public InterpreterValue callMethod(InterpreterValue receiver, String methodName, List<InterpreterValue> arguments) {
        val method = findMethod(methods, methodName, arguments);
        return method.apply((T)receiver, new Arguments(arguments));
    }

    public InterpreterValue callStaticMethod(Environment environment, String methodName, List<InterpreterValue> arguments) {
        val method = findMethod(staticMethods, methodName, arguments);
        return method.apply(environment, new Arguments(arguments));
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
