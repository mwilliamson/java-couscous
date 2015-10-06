package org.zwobble.couscous.interpreter.values;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.zwobble.couscous.ast.ClassName;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.Executor;
import org.zwobble.couscous.interpreter.NoSuchMethod;
import org.zwobble.couscous.interpreter.UnexpectedValueType;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;
import org.zwobble.couscous.values.TypeReference;

import com.google.common.collect.ImmutableMap;

import static java.util.stream.Collectors.toMap;
import static org.zwobble.couscous.values.TypeReference.typeRef;

import lombok.val;

public class ConcreteType<T> {
    public static class Builder<T> {
        private final ImmutableMap.Builder<String, MethodValue<T>> methods =
            ImmutableMap.builder();
        private final ImmutableMap.Builder<String, StaticMethodValue> staticMethods =
            ImmutableMap.builder();
        private final ClassName name;
        
        public Builder(ClassName name) {
            this.name = name;
        }
        
        public Builder<T> method(
                String name,
                List<TypeReference> argumentsTypes,
                BiFunction<T, Arguments, InterpreterValue> method) {
            methods.put(name, new MethodValue<T>(argumentsTypes, method));
            return this;
        }
        
        public Builder<T> staticMethod(
                String name,
                List<TypeReference> argumentsTypes,
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
                    List<TypeReference> argumentTypes = method.getArguments()
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

    private ClassName name;
    private Map<String, MethodValue<T>> methods;
    private Map<String, StaticMethodValue> staticMethods;

    public ConcreteType(
            ClassName name,
            Map<String, MethodValue<T>> methods,
            Map<String, StaticMethodValue> staticMethods) {
        this.name = name;
        this.methods = methods;
        this.staticMethods = staticMethods;
    }
    
    public ClassName getName() {
        return name;
    }

    public static <T> ConcreteType.Builder<T> builder(TypeReference reference) {
        return builder(reference.getName());
    }

    public static <T> ConcreteType.Builder<T> builder(String name) {
        return new Builder<>(ClassName.of(name));
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
            if (!formalArgumentType.equals(actualArgumentType.getReference())) {
                throw new UnexpectedValueType(formalArgumentType, actualArgumentType.getReference());
            }
        }
        return method;
    }
    
    public TypeReference getReference() {
        return typeRef(name.getQualifiedName());
    }

    @Override
    public String toString() {
        return "ConcreteType<" + name + ">";
    }
}
