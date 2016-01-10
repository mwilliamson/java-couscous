package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.TypeName;

public final class StringValue implements PrimitiveValue {
    
    public static StringValue of(String value) {
        return new StringValue(value);
    }
    public static final TypeName REF = TypeName.of("java.lang.String");
    private final String value;
    
    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visitString(value);
    }
    
    @Override
    public TypeName getType() {
        return REF;
    }
    
    private StringValue(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof StringValue)) return false;
        final StringValue other = (StringValue)o;
        final java.lang.Object this$value = this.getValue();
        final java.lang.Object other$value = other.getValue();
        if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "StringValue(value=" + this.getValue() + ")";
    }
}