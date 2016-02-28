package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.Optional;

public interface InterpreterValue {
    InterpreterType getType();
    Optional<PrimitiveValue> toPrimitiveValue();
    InterpreterValue getField(String fieldName);
    void setField(String fieldName, InterpreterValue value);
}
