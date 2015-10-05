package org.zwobble.couscous.values;

public class UnitValue implements PrimitiveValue {
    static final UnitValue UNIT = new UnitValue();
    
    private UnitValue() {
    }
    
    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
