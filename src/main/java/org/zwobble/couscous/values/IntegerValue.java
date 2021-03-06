package org.zwobble.couscous.values;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;

public final class IntegerValue implements PrimitiveValue {
    private final int value;
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitInteger(value);
    }
    
    @Override
    public ScalarType getType() {
        return Types.INT;
    }
    
    public IntegerValue(final int value) {
        this.value = value;
    }
    
    public int getValue() {
        return this.value;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof IntegerValue)) return false;
        final IntegerValue other = (IntegerValue)o;
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
        return "IntegerValue(value=" + this.getValue() + ")";
    }
}