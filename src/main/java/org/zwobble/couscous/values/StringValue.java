package org.zwobble.couscous.values;

import java.util.List;

import lombok.Value;
import lombok.val;

@Value
public class StringValue implements InterpreterValue {
    String value;

    @Override
    public InterpreterValue callMethod(String methodName, List<InterpreterValue> arguments) {
        if (methodName == "length") {
            return new IntegerValue(value.length());
        } else if (methodName == "substring") {
            val startIndex = (IntegerValue)arguments.get(0);
            val endIndex = (IntegerValue)arguments.get(1);
            return new StringValue(value.substring(startIndex.getValue(), endIndex.getValue()));
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
