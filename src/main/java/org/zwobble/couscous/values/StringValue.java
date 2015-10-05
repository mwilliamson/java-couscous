package org.zwobble.couscous.values;

import static org.zwobble.couscous.values.TypeReference.typeRef;

import lombok.Value;

@Value
public class StringValue implements PrimitiveValue {
    public static final TypeReference REF = typeRef("java.lang.String");

    String value;

    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public TypeReference getType() {
        return REF;
    }
}
