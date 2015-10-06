package org.zwobble.couscous.interpreter.values;

import java.util.Optional;

import org.zwobble.couscous.values.PrimitiveValue;

public interface InterpreterValue {
    ConcreteType getType();
    Optional<PrimitiveValue> toPrimitiveValue();
    InterpreterValue getField(String fieldName);
}
