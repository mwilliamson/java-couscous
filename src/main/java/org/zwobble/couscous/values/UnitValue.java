package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.TypeName;

public class UnitValue implements PrimitiveValue {
    public static final TypeName REF = TypeName.of("org.zwobble.couscous.runtime.Unit");
    
    static final UnitValue UNIT = new UnitValue();
    
    private UnitValue() {
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
