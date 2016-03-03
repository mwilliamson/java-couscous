package org.zwobble.couscous.interpreter.types;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.PositionalArguments;
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
    public static <T> Builder<T> builder(Class<T> interpreterValueType, TypeName reference) {
        return new Builder<>(interpreterValueType, reference);
    }

    public static <T> Builder<T> builder(Class<T> interpreterValueType, String name) {
        return builder(interpreterValueType, TypeName.of(name));
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
        private final TypeName name;

        public Builder(Class<T> interpreterValueType, TypeName name) {
            this.interpreterValueType = interpreterValueType;
            this.name = name;
        }

        public Builder<T> field(String name, TypeName type) {
            fields.put(name, FieldDeclarationNode.field(name, type));
            return this;
        }

        public Builder<T> constructor(List<TypeName> argumentsTypes, BiFunction<Environment, MethodCallArguments<T>, InterpreterValue> method) {
            constructor = toMethodValue(argumentsTypes, method);
            return this;
        }

        public Builder<T> method(
            String name,
            List<TypeName> argumentsTypes,
            TypeName returnType,
            BiFunction<Environment, MethodCallArguments<T>, InterpreterValue> method)
        {
            methods.put(new MethodSignature(name, argumentsTypes, returnType), toMethodValue(argumentsTypes, method));
            return this;
        }

        private MethodValue toMethodValue(List<TypeName> argumentsTypes, BiFunction<Environment, MethodCallArguments<T>, InterpreterValue> method) {
            return new MethodValue(
                argumentsTypes,
                (environment, arguments) ->
                    tryCast(interpreterValueType, arguments.getReceiver())
                        .map(typedReceiver -> method.apply(environment, MethodCallArguments.of(typedReceiver, arguments.getPositionalArguments())))
                        .orElseThrow(() -> new RuntimeException("receiver is of wrong type")));
        }

        public Builder<T> staticMethod(
            String name,
            List<TypeName> argumentsTypes,
            TypeName returnType,
            BiFunction<Environment, PositionalArguments, InterpreterValue> method)
        {
            staticMethods.put(new MethodSignature(name, argumentsTypes, returnType), new StaticMethodValue(argumentsTypes, method));
            return this;
        }

        public InterpreterType build() {
            return new IntrinsicInterpreterType(
                name,
                fields.build(),
                constructor,
                methods.build(),
                staticMethods.build());
        }
    }

    private final TypeName name;
    private final Map<String, FieldDeclarationNode> fields;
    private final MethodValue constructor;
    private final Map<MethodSignature, MethodValue> methods;
    private final Map<MethodSignature, StaticMethodValue> staticMethods;

    private IntrinsicInterpreterType(
        TypeName name,
        Map<String, FieldDeclarationNode> fields,
        MethodValue constructor,
        Map<MethodSignature, MethodValue> methods,
        Map<MethodSignature, StaticMethodValue> staticMethods)
    {
        this.name = name;
        this.fields = fields;
        this.constructor = constructor;
        this.methods = methods;
        this.staticMethods = staticMethods;
    }

    @Override
    public TypeName getName() {
        return name;
    }

    @Override
    public Set<TypeName> getSuperTypes() {
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
        return method.apply(environment, MethodCallArguments.of(receiver, new PositionalArguments(arguments)));
    }

    @Override
    public InterpreterValue callStaticMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments) {
        StaticMethodValue method = findMethod(staticMethods, signature);
        return method.apply(environment, new PositionalArguments(arguments));
    }

    private static <T extends Callable> T findMethod(Map<MethodSignature, T> methods, MethodSignature signature) {
        if (!methods.containsKey(signature)) {
            throw new NoSuchMethod(signature);
        }
        return methods.get(signature);
    }

    @Override
    public InterpreterValue callConstructor(Environment environment, List<InterpreterValue> arguments) {
        ObjectInterpreterValue object = new ObjectInterpreterValue(this);
        constructor.apply(environment, MethodCallArguments.of(object, new PositionalArguments(arguments)));
        return object;
    }

    @Override
    public String toString() {
        return "IntrinsicInterpreterType<" + name + ">";
    }
}
