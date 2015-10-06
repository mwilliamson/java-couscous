package org.zwobble.couscous.interpreter.values;

import java.util.Optional;

import org.zwobble.couscous.interpreter.NoSuchField;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class BooleanInterpreterValue implements InterpreterValue {
    private static final ConcreteType TYPE = ConcreteType.builder(BooleanInterpreterValue.class, "Boolean")
        .build();
    
    @Getter(value = AccessLevel.NONE)
    boolean value;

    public boolean getValue() {
        return value;
    }

    @Override
    public ConcreteType getType() {
        return TYPE;
    }

    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.of(PrimitiveValues.value(value));
    }

    @Override
    public InterpreterValue getField(String fieldName) {
        throw new NoSuchField(fieldName);
    }
}
