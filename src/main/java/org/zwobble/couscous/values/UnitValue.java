package org.zwobble.couscous.values;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;

public class UnitValue implements PrimitiveValue {
    static final UnitValue UNIT = new UnitValue();
    
    private UnitValue() {
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnit();
    }

    @Override
    public ScalarType getType() {
        return Types.VOID;
    }
}
