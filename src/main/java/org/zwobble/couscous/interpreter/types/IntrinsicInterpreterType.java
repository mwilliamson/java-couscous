package org.zwobble.couscous.interpreter.types;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.errors.NoSuchMethod;
import org.zwobble.couscous.interpreter.values.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.ExtraSets.set;

public class IntrinsicInterpreterType implements InterpreterType {
    public static <T> Builder<T> builder(Class<T> interpreterValueType, Type reference) {
        return new Builder<>(interpreterValueType, reference);
    }

    public static <T> Builder<T> builder(Class<T> interpreterValueType, String name) {
        return builder(interpreterValueType, ScalarType.of(name));
    }

    public static Builder<ObjectInterpreterValue> classBuilder(String name) {
        return builder(ObjectInterpreterValue.class, name);
    }

    public static class Builder<T> {
        private final ImmutableMap.Builder<String, FieldDeclarationNode> fields = ImmutableMap.builder();
        private MethodValue constructor = new MethodValue(list(), (environment, arguments) -> InterpreterValues.UNIT);
        private final ImmutableMap.Builder<MethodSignature, MethodValue> methods = ImmutableMap.builder();
        private final ImmutableMap.Builder<MethodSignature, StaticMethodValue> staticMethods = ImmutableMap.builder();
        private final Class<T> interpreterValueType;
        private final Type type;

        public Builder(Class<T> interpreterValueType, Type type) {
            this.interpreterValueType = interpreterValueType;
            this.type = type;
        }

        public Builder<T> field(String name, ScalarType type) {
            fields.put(name, FieldDeclarationNode.field(name, type));
            return this;
        }

        public Builder<T> constructor(List<Type> argumentsTypes, BiFunction<Environment, MethodCallArguments<T>, InterpreterValue> method) {
            constructor = toMethodValue(argumentsTypes, method);
            return this;
        }

        public Builder<T> method(
            String name,
            List<Type> argumentsTypes,
            Type returnType,
            BiFunction<Environment, MethodCallArguments<T>, InterpreterValue> method)
        {
            methods.put(new MethodSignature(name, argumentsTypes, returnType), toMethodValue(argumentsTypes, method));
            return this;
        }

        private MethodValue toMethodValue(List<Type> argumentsTypes, BiFunction<Environment, MethodCallArguments<T>, InterpreterValue> method) {
            return new MethodValue(
                argumentsTypes,
                (environment, arguments) ->
                    tryCast(interpreterValueType, arguments.getReceiver())
                        .map(typedReceiver -> method.apply(environment, MethodCallArguments.of(typedReceiver, arguments.getArguments())))
                        .orElseThrow(() -> new RuntimeException("receiver is of wrong type")));
        }

        public Builder<T> staticMethod(
            String name,
            List<Type> argumentsTypes,
            Type returnType,
            BiFunction<Environment, Arguments, InterpreterValue> method)
        {
            staticMethods.put(new MethodSignature(name, argumentsTypes, returnType), new StaticMethodValue(argumentsTypes, method));
            return this;
        }

        public InterpreterType build() {
            return new IntrinsicInterpreterType(
                type,
                fields.build(),
                constructor,
                methods.build(),
                staticMethods.build());
        }
    }

    private final Type type;
    private final Map<String, FieldDeclarationNode> fields;
    private final MethodValue constructor;
    private final Map<MethodSignature, MethodValue> methods;
    private final Map<MethodSignature, StaticMethodValue> staticMethods;

    private IntrinsicInterpreterType(
        Type type,
        Map<String, FieldDeclarationNode> fields,
        MethodValue constructor,
        Map<MethodSignature, MethodValue> methods,
        Map<MethodSignature, StaticMethodValue> staticMethods)
    {
        this.type = type;
        this.fields = fields;
        this.constructor = constructor;
        this.methods = methods;
        this.staticMethods = staticMethods;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Type> getSuperTypes() {
        return set();
    }

    @Override
    public Optional<FieldDeclarationNode> getField(String fieldName) {
        return Optional.ofNullable(fields.get(fieldName));
    }

    @Override
    public List<StatementNode> getStaticConstructor() {
        return list();
    }

    @Override
    public InterpreterValue callMethod(Environment environment, InterpreterValue receiver, MethodSignature signature, List<InterpreterValue> arguments) {
        MethodValue method = findMethod(methods, signature);
        return method.apply(environment, MethodCallArguments.of(receiver, new Arguments(arguments)));
    }

    @Override
    public InterpreterValue callStaticMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments) {
        StaticMethodValue method = findMethod(staticMethods, signature);
        return method.apply(environment, new Arguments(arguments));
    }

    private static <T extends Callable> T findMethod(Map<MethodSignature, T> methods, MethodSignature signature) {
        if (!methods.containsKey(signature)) {
            throw new NoSuchMethod(signature);
        }
        return methods.get(signature);
    }

    @Override
    public void callConstructor(Environment environment, InterpreterValue thisValue, List<InterpreterValue> arguments) {
        constructor.apply(environment, MethodCallArguments.of(thisValue, new Arguments(arguments)));
    }

    @Override
    public String toString() {
        return "IntrinsicInterpreterType<" + type + ">";
    }
}
