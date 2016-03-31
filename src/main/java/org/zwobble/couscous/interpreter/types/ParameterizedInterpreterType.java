package org.zwobble.couscous.interpreter.types;

import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.zwobble.couscous.types.ParameterizedType.parameterizedType;

public class ParameterizedInterpreterType implements InterpreterType {
    private final InterpreterType rawType;
    private final List<Type> parameters;

    public ParameterizedInterpreterType(InterpreterType rawType, List<Type> parameters) {
        this.rawType = rawType;
        this.parameters = parameters;
    }

    @Override
    public Type getType() {
        return parameterizedType((ScalarType) rawType.getType(), parameters);
    }

    // TODO: implement the other methods properly.
    // Possibly worth splitting off a separate notion of interpreter type.
    // For instance, callConstructor is arguably not relevant here.
    // (Or that we should be instantiating this type before the call to callConstructor)

    @Override
    public Set<Type> getSuperTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<FieldDeclarationNode> getField(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<StatementNode> getStaticConstructor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void callConstructor(Environment environment, InterpreterValue thisValue, List<InterpreterValue> arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InterpreterValue callMethod(Environment environment, InterpreterValue value, MethodSignature signature, List<InterpreterValue> arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InterpreterValue callStaticMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments) {
        throw new UnsupportedOperationException();
    }
}
