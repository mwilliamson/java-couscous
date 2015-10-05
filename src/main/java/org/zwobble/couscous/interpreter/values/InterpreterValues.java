package org.zwobble.couscous.interpreter.values;

public class InterpreterValues {
    public static final UnitValue UNIT = UnitValue.UNIT;
    
    public static InterpreterValue value(int value) {
        return new IntegerValue(value);
    }
    
    public static InterpreterValue value(String value) {
        return new StringValue(value);
    }
}
