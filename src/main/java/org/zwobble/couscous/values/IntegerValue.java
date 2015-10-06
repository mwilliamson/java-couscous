package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.TypeName;

import lombok.Value;

@Value
public class IntegerValue implements PrimitiveValue {
    public static final TypeName REF = TypeName.of("java.lang.Integer");
    
    int value;

    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public TypeName getType() {
        return REF;
    }
}
