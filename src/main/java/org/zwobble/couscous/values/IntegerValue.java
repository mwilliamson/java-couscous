package org.zwobble.couscous.values;

import static org.zwobble.couscous.values.TypeReference.typeRef;

import lombok.Value;

@Value
public class IntegerValue implements PrimitiveValue {
    public static final TypeReference REF = typeRef("java.lang.Integer");
    
    int value;

    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
