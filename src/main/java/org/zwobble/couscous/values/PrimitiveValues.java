package org.zwobble.couscous.values;

public class PrimitiveValues {
    public static final PrimitiveValue UNIT = UnitValue.UNIT;
    
    public static PrimitiveValue value(String value) {
        return new StringValue(value);
    }
    
    public static PrimitiveValue value(int value) {
        return new IntegerValue(value);
    }
    
    public static PrimitiveValue value(boolean value) {
        return new BooleanValue(value);
    }
}
