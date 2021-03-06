package org.zwobble.couscous.values;

import org.zwobble.couscous.types.ScalarType;

public class PrimitiveValues {
    public static final PrimitiveValue UNIT = UnitValue.UNIT;
    public static final PrimitiveValue TRUE = new BooleanValue(true);
    public static final PrimitiveValue FALSE = new BooleanValue(false);
    
    public static PrimitiveValue value(String value) {
        return StringValue.of(value);
    }
    
    public static PrimitiveValue value(int value) {
        return new IntegerValue(value);
    }

    public static PrimitiveValue value(char value) {
        return new CharValue(value);
    }
    
    public static PrimitiveValue value(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static PrimitiveValue value(ScalarType type) {
        return new TypeValue(type);
    }
}
