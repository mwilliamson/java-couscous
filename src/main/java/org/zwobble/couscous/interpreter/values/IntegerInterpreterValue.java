package org.zwobble.couscous.interpreter.values;

import java.util.Optional;
import org.zwobble.couscous.interpreter.NoSuchField;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;
import org.zwobble.couscous.values.StringValue;
import static java.util.Arrays.asList;

public final class IntegerInterpreterValue implements InterpreterValue {
    public static final ConcreteType TYPE = ConcreteType.builder(IntegerInterpreterValue.class, IntegerValue.REF).staticMethod("parseInt", asList(StringValue.REF), (environment, arguments) -> {
        StringInterpreterValue value = (StringInterpreterValue)arguments.get(0);
        return new IntegerInterpreterValue(Integer.parseInt(value.getValue()));
    }).build();
    private final int value;
    
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
    
    public IntegerInterpreterValue(final int value) {
        this.value = value;
    }
    
    public int getValue() {
        return this.value;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof IntegerInterpreterValue)) return false;
        final IntegerInterpreterValue other = (IntegerInterpreterValue)o;
        if (this.getValue() != other.getValue()) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getValue();
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "IntegerInterpreterValue(value=" + this.getValue() + ")";
    }
}