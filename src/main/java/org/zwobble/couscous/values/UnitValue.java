package org.zwobble.couscous.values;

import lombok.ToString;

@ToString
public class UnitValue implements InterpreterValue {
    public static final UnitValue UNIT = new UnitValue(); 
    
    private UnitValue() {
    }

    @Override
    public InterpreterValue callMethod(String methodName) {
        throw new UnsupportedOperationException();
    }
}
