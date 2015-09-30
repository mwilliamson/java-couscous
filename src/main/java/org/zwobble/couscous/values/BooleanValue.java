package org.zwobble.couscous.values;

import java.util.Optional;

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
    public Optional<MethodValue> getMethod(String methodName) {
        throw new UnsupportedOperationException();
    }
}
