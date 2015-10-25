package org.zwobble.couscous.interpreter.values;

import java.util.Optional;
import org.zwobble.couscous.interpreter.NoSuchField;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

public final class BooleanInterpreterValue implements InterpreterValue {
    private static final ConcreteType TYPE = ConcreteType.builder(BooleanInterpreterValue.class, "Boolean").build();
    private final boolean value;
    
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
    
    @Override
    public void setField(String fieldName, InterpreterValue value) {
        throw new NoSuchField(fieldName);
    }
    
    public BooleanInterpreterValue(final boolean value) {
        this.value = value;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof BooleanInterpreterValue)) return false;
        final BooleanInterpreterValue other = (BooleanInterpreterValue)o;
        if (this.getValue() != other.getValue()) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.getValue() ? 79 : 97);
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "BooleanInterpreterValue(value=" + this.getValue() + ")";
    }
}