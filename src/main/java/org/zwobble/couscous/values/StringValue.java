package org.zwobble.couscous.values;

import lombok.Value;

@Value
public class StringValue implements InterpreterValue {
    String value;
}
