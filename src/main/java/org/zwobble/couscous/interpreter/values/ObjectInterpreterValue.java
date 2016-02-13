package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.values.PrimitiveValue;

import java.util.Optional;

public class ObjectInterpreterValue implements InterpreterValue {
    private final ConcreteType type;
    private final InterpreterFields fields;
    
    public ObjectInterpreterValue(ConcreteType type) {
        this.type = type;
        this.fields = InterpreterFields.forInstanceOf(type);
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
        return fields.getField(fieldName);
    }
    
    @Override
    public void setField(String fieldName, InterpreterValue value) {
        fields.setField(fieldName, value);
    }
}