package org.zwobble.couscous.values;

import java.util.Optional;

import lombok.ToString;

@ToString
public class UnitValue implements InterpreterValue {
    public static final UnitValue UNIT = new UnitValue(); 
    
    private UnitValue() {
    }

    @Override
    public Optional<MethodValue> getMethod(String methodName) {
        throw new UnsupportedOperationException();
    }
}
