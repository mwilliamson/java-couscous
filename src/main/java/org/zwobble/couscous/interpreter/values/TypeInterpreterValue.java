package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.values.ObjectValues;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

import java.util.Optional;

public class TypeInterpreterValue implements InterpreterValue {
    public static final ConcreteType TYPE = ConcreteType.builder(TypeInterpreterValue.class, ObjectValues.CLASS)
        .build();

    public static InterpreterValue of(TypeName type) {
        return new TypeInterpreterValue(type);
    }

    private final TypeName type;

    public TypeInterpreterValue(TypeName type) {
        this.type = type;
    }

    @Override
    public ConcreteType getType() {
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
