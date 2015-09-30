package org.zwobble.couscous.values;

import lombok.Value;

@Value
public class IntegerValue implements InterpreterValue {
    private static final ConcreteType<?> TYPE = ConcreteType.<IntegerValue>builder()
        .build();
    
    int value;

    @Override
    public ConcreteType<?> getType() {
        return TYPE;
    }
}
