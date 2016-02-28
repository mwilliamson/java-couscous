package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.Optional;

public class ObjectInterpreterValue implements InterpreterValue {
    private final InterpreterType type;
    private final InterpreterFields fields;
    
    public ObjectInterpreterValue(InterpreterType type) {
        this.type = type;
        this.fields = InterpreterFields.forInstanceOf(type);
    }
    
    @Override
    public InterpreterType getType() {
        return type;
    }
    
    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.empty();
    }
    
    @Override
    public InterpreterValue getField(String fieldName) {
        return fields.getField(fieldName);
    }
    
    @Override
    public void setField(String fieldName, InterpreterValue value) {
        fields.setField(fieldName, value);
    }
}