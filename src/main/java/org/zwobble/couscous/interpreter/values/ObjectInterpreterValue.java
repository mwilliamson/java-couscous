package org.zwobble.couscous.interpreter.values;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.zwobble.couscous.interpreter.InterpreterTypes;
import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.interpreter.errors.UnboundField;
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
        type.getField(fieldName).orElseThrow(() -> new NoSuchField(fieldName));
        if (fields.containsKey(fieldName)) {
            return fields.get(fieldName);
        } else {
            throw new UnboundField(fieldName);
        }
    }
    
    @Override
    public void setField(String fieldName, InterpreterValue value) {
        FieldValue field = type.getField(fieldName)
            .orElseThrow(() -> new NoSuchField(fieldName));
        InterpreterTypes.checkIsInstance(field.getType(), value);
        fields.put(fieldName, value);
    }
}