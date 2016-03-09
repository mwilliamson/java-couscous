package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.values.ObjectValues;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

import java.util.Optional;

public class TypeInterpreterValue implements InterpreterValue {
    public static final InterpreterType TYPE = IntrinsicInterpreterType
        .builder(TypeInterpreterValue.class, ObjectValues.CLASS)
        .build();

    public static InterpreterValue of(ScalarType type) {
        return new TypeInterpreterValue(type);
    }

    private final ScalarType type;

    public TypeInterpreterValue(ScalarType type) {
        this.type = type;
    }

    @Override
    public InterpreterType getType() {
        return TYPE;
    }

    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.of(PrimitiveValues.value(type));
    }

    @Override
    public InterpreterValue getField(String fieldName) {
        throw new NoSuchField(fieldName);
    }

    @Override
    public void setField(String fieldName, InterpreterValue value) {
        throw new NoSuchField(fieldName);
    }
}
