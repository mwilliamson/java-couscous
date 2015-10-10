package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.TypeName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class BooleanValue implements PrimitiveValue {
    public static final TypeName REF = TypeName.of("boolean");
    
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
    public TypeName getType() {
        return REF;
    }
}
