package org.zwobble.couscous.values;

import java.util.Optional;

import lombok.Value;
import lombok.val;

@Value
public class StringValue implements InterpreterValue {
    String value;

    @Override
    public Optional<MethodValue> getMethod(String methodName) {
        if (methodName == "length") {
            return Optional.of(arguments -> new IntegerValue(value.length()));
        } else if (methodName == "substring") {
            return Optional.of(arguments -> {
                val startIndex = (IntegerValue)arguments.get(0);
                val endIndex = (IntegerValue)arguments.get(1);
                return new StringValue(value.substring(startIndex.getValue(), endIndex.getValue()));
            });
        } else {
            return Optional.empty();
        }
    }
}
