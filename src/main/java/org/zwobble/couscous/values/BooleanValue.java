package org.zwobble.couscous.values;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class BooleanValue implements InterpreterValue {
    private static final ConcreteType<BooleanValue> TYPE = ConcreteType.<BooleanValue>builder("Boolean")
        .build();
    
    @Getter(value = AccessLevel.NONE)
    boolean value;

    public boolean getValue() {
        return value;
    }

    @Override
    public ConcreteType<?> getType() {
        return TYPE;
    }
}
