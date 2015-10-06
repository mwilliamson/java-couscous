package org.zwobble.couscous.interpreter.values;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.zwobble.couscous.values.PrimitiveValue;

public class ObjectInterpreterValue implements InterpreterValue {
    private final ConcreteType type;
    private final Map<String, InterpreterValue> fields;

    public ObjectInterpreterValue(ConcreteType type) {
        this.type = type;
        this.fields = new HashMap<>();
    }
    
    @Override
    public ConcreteType getType() {
        return type;
    }

    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.empty();
    }

    @Override
    public InterpreterValue getField(String fieldName) {
        if (fields.containsKey(fieldName)) {
            return fields.get(fieldName);            
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void setField(String fieldName, InterpreterValue value) {
        fields.put(fieldName, value);
    }
}
