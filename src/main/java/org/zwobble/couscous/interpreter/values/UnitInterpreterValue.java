package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

import java.util.Optional;

public class UnitInterpreterValue implements InterpreterValue {
    private static final InterpreterType TYPE = IntrinsicInterpreterType.builder(UnitInterpreterValue.class, "Unit").build();
    public static final UnitInterpreterValue UNIT = new UnitInterpreterValue();
    
    private UnitInterpreterValue() {
    }
    
    @Override
    public InterpreterType getType() {
        return TYPE;
    }
    
    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.of(PrimitiveValues.UNIT);
    }
    
    @Override
    public InterpreterValue getField(String fieldName) {
        throw new NoSuchField(fieldName);
    }
    
    @Override
    public void setField(String fieldName, InterpreterValue value) {
        throw new NoSuchField(fieldName);
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "UnitInterpreterValue()";
    }
}