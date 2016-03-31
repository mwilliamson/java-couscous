package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.List;
import java.util.Optional;

public class ArrayInterpreterValue implements InterpreterValue {
    private final Type elementType;
    private final List<InterpreterValue> elements;

    public ArrayInterpreterValue(Type elementType, List<InterpreterValue> elements) {
        this.elementType = elementType;
        this.elements = elements;
    }

    public Type getElementType() {
        return elementType;
    }

    public List<InterpreterValue> getElements() {
        return elements;
    }

    @Override
    public InterpreterType getType() {
        return IntrinsicInterpreterType.builder(ArrayInterpreterValue.class, Types.array(elementType))
            .build();
    }

    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.empty();
    }

    @Override
    public InterpreterValue getField(String fieldName) {
        throw new NoSuchField(fieldName);
    }

    @Override
    public void setField(String fieldName, InterpreterValue value) {
        throw new NoSuchField(fieldName);
    }
}
