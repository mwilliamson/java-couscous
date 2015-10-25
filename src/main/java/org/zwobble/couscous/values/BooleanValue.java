package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.TypeName;

public final class BooleanValue implements PrimitiveValue {
    public static final TypeName REF = TypeName.of("boolean");
    private final boolean value;
    
    public boolean getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
    @Override
    public TypeName getType() {
        return REF;
    }
    
    public BooleanValue(final boolean value) {
        this.value = value;
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