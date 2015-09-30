package org.zwobble.couscous.values;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class BooleanValue implements InterpreterValue {
    @Getter(value = AccessLevel.NONE)
    boolean value;

    public boolean getValue() {
        return value;
    }

    @Override
    public InterpreterValue callMethod(String methodName, List<InterpreterValue> arguments) {
        throw new UnsupportedOperationException();
    }
}
