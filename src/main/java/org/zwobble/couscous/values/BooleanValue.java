package org.zwobble.couscous.values;

import static org.zwobble.couscous.values.TypeReference.typeRef;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class BooleanValue implements PrimitiveValue {
    public static final TypeReference REF = typeRef("java.lang.Boolean");
    
    @Getter(value = AccessLevel.NONE)
    boolean value;
    
    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public TypeReference getType() {
        return REF;
    }
}
