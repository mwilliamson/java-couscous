package org.zwobble.couscous.values;

import java.util.Optional;

import lombok.Value;

@Value
public class IntegerValue implements InterpreterValue {
    int value;

    @Override
    public Optional<MethodValue> getMethod(String methodName) {
        throw new UnsupportedOperationException();
    }
}
