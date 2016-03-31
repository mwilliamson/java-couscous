package org.zwobble.couscous.interpreter.types;

import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.types.ParameterizedType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.TypeParameter;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.util.ExtraIterables;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.zwobble.couscous.types.ParameterizedType.parameterizedType;
import static org.zwobble.couscous.util.ExtraMaps.toMap;

public class ParameterizedInterpreterType implements InterpreterType {
    private final InterpreterType genericType;
    private final List<Type> parameters;

    public ParameterizedInterpreterType(InterpreterType genericType, List<Type> parameters) {
        this.genericType = genericType;
        this.parameters = parameters;
    }

    @Override
    public Type getType() {
        return parameterizedType(((ParameterizedType) genericType.getType()).getRawType(), parameters);
    }

    // TODO: implement the other methods properly.
    // Possibly worth splitting off a separate notion of interpreter type.
    // For instance, callConstructor is arguably not relevant here.
    // (Or that we should be instantiating this type before the call to callConstructor)

    @Override
    public Set<Type> getSuperTypes() {
        Map<TypeParameter, Type> replacements = toMap(
            ExtraIterables.cast(TypeParameter.class, ((ParameterizedType) genericType.getType()).getParameters()),
            parameters);
        return genericType.getSuperTypes()
            .stream()
            .map(type -> Types.substitute(type, replacements))
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<FieldDeclarationNode> getField(String fieldName) {
        return genericType.getField(fieldName);
    }

    @Override
    public List<StatementNode> getStaticConstructor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void callConstructor(Environment environment, InterpreterValue thisValue, Arguments arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InterpreterValue callMethod(Environment environment, InterpreterValue value, MethodSignature signature, Arguments arguments) {
        return genericType.callMethod(environment, value, signature.generic(), arguments);
    }

    @Override
    public InterpreterValue callStaticMethod(Environment environment, MethodSignature signature, Arguments arguments) {
        throw new UnsupportedOperationException();
    }
}
