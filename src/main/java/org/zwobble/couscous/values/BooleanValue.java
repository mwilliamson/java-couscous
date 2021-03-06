package org.zwobble.couscous.values;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;

public final class BooleanValue implements PrimitiveValue {
    private final boolean value;

    public BooleanValue(final boolean value) {
        this.value = value;
    }
    
    public boolean getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBoolean(value);
    }
    
    @Override
    public ScalarType getType() {
        return Types.BOOLEAN;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof BooleanValue)) return false;
        final BooleanValue other = (BooleanValue)o;
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
        return "BooleanValue(value=" + this.getValue() + ")";
    }
}