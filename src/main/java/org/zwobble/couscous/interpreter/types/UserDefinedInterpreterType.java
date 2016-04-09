package org.zwobble.couscous.interpreter.types;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.Executor;
import org.zwobble.couscous.interpreter.InterpreterTypes;
import org.zwobble.couscous.interpreter.errors.NoSuchMethod;
import org.zwobble.couscous.interpreter.errors.WrongNumberOfArguments;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static org.zwobble.couscous.types.ParameterizedType.parameterizedType;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.lazyFlatMap;
import static org.zwobble.couscous.util.ExtraIterables.lazyMap;
import static org.zwobble.couscous.util.ExtraLists.*;
import static org.zwobble.couscous.util.ExtraMaps.*;

public class UserDefinedInterpreterType implements InterpreterType {
    private final TypeNode type;
    private final Map<String, FieldDeclarationNode> fields;
    private final Map<MethodSignature, MethodNode> methods;

    public UserDefinedInterpreterType(TypeNode type) {
        this.type = type;
        this.fields = tryCast(ClassNode.class, type)
            .map(classNode -> toMapWithKeys(classNode.getFields(), FieldDeclarationNode::getName))
            .orElse(map());

        Iterable<MethodNode> concreteMethods = filter(type.getMethods(), method -> !method.isAbstract());

        Iterable<Map.Entry<MethodSignature, MethodNode>> entries = lazyFlatMap(concreteMethods, method -> {
            List<MethodSignature> signatures = cons(method.signature(), method.getOverrides());
            return lazyMap(signatures, signature -> entry(signature.generic(), method));
        });
        this.methods = toMap(entries);
    }

    @Override
    public Type getType() {
        List<Type> parameters = eagerMap(
            type.getTypeParameters(),
            parameter -> parameter.getType());
        return parameters.isEmpty()
            ? type.getName()
            : parameterizedType(type.getName(), parameters);
    }

    @Override
    public Set<Type> getSuperTypes() {
        return type.getSuperTypes();
    }

    @Override
    public Optional<FieldDeclarationNode> getField(String fieldName) {
        return Optional.ofNullable(fields.get(fieldName));
    }

    @Override
    public List<StatementNode> getStaticConstructor() {
        return tryCast(ClassNode.class, type)
            .map(node -> node.getStaticConstructor())
            .orElse(list());
    }

    @Override
    public void callConstructor(Environment environment, InterpreterValue thisValue, Arguments arguments) {
        ConstructorNode constructor = tryCast(ClassNode.class, type)
            // TODO: add test for this case
            .orElseThrow(() -> new RuntimeException("Cannot instantiate non-class types"))
            .getConstructor();
        List<Type> formalArgumentTypes = eagerMap(
            constructor.getArguments(),
            FormalArgumentNode::getType);
        checkMethodArguments(formalArgumentTypes, arguments.getValues());
        Executor.callConstructor(
            environment,
            constructor,
            thisValue,
            arguments);
    }

    private static void checkMethodArguments(final List<Type> argumentTypes, List<InterpreterValue> arguments) {
        if (argumentTypes.size() != arguments.size()) {
            throw new WrongNumberOfArguments(argumentTypes.size(), arguments.size());
        }
        for (int index = 0; index < arguments.size(); index++) {
            Type formalArgumentType = argumentTypes.get(index);
            InterpreterTypes.checkIsInstance(formalArgumentType, arguments.get(index));
        }
    }

    @Override
    public InterpreterValue callMethod(Environment environment, InterpreterValue value, MethodSignature signature, Arguments arguments) {
        return Executor.callMethod(
            environment,
            findMethod(signature, false),
            Optional.of(value),
            arguments);
    }

    @Override
    public InterpreterValue callStaticMethod(Environment environment, MethodSignature signature, Arguments arguments) {
        return Executor.callMethod(
            environment,
            findMethod(signature, true),
            Optional.empty(),
            arguments);
    }

    private MethodNode findMethod(MethodSignature signature, boolean isStatic) {
        return Optional.ofNullable(methods.get(signature))
            .filter(method -> method.isStatic() == isStatic)
            .orElseThrow(() -> new NoSuchMethod(signature));
    }
}
