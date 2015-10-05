package org.zwobble.couscous.values;

public class UnitValue implements PrimitiveValue {
    public static final TypeReference REF = TypeReference.typeRef("org.zwobble.couscous.runtime.Unit");
    
    static final UnitValue UNIT = new UnitValue();
    
    private UnitValue() {
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
