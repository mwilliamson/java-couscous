package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.types.ScalarType;

public class UnitValue implements PrimitiveValue {
    public static final ScalarType REF = ScalarType.of("void");
    
    static final UnitValue UNIT = new UnitValue();
    
    private UnitValue() {
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnit();
    }

    @Override
    public ScalarType getType() {
        return REF;
    }
}
