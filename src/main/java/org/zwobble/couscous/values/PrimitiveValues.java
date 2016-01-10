package org.zwobble.couscous.values;

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
    
    public static PrimitiveValue value(boolean value) {
        return value ? TRUE : FALSE;
    }
}
