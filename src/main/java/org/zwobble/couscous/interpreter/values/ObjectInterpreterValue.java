package org.zwobble.couscous.interpreter.values;

import java.util.Optional;

import org.zwobble.couscous.values.PrimitiveValue;

public class ObjectInterpreterValue implements InterpreterValue {
    private final ConcreteType type;

    public ObjectInterpreterValue(ConcreteType type) {
        this.type = type;
    }
    
    @Override
    public ConcreteType getType() {
        return type;
    }

    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.empty();
    }
}
