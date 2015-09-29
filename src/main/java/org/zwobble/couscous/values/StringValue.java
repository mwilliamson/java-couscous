package org.zwobble.couscous.values;

import lombok.Value;

@Value
public class StringValue implements InterpreterValue {
    String value;

    @Override
    public InterpreterValue callMethod(String methodName) {
        if (methodName == "length") {
            return new IntegerValue(value.length());
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
