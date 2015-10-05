package org.zwobble.couscous.values;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class BooleanValue implements PrimitiveValue {
    @Getter(value = AccessLevel.NONE)
    boolean value;
    
    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
