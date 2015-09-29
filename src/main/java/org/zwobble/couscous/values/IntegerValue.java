package org.zwobble.couscous.values;

import lombok.Value;

@Value
public class IntegerValue implements InterpreterValue {
    int value;
}
