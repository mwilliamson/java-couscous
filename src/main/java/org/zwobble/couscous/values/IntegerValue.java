package org.zwobble.couscous.values;

import java.util.List;

import lombok.Value;

@Value
public class IntegerValue implements InterpreterValue {
    int value;

    @Override
    public InterpreterValue callMethod(String methodName, List<InterpreterValue> arguments) {
        throw new UnsupportedOperationException();
    }
}
