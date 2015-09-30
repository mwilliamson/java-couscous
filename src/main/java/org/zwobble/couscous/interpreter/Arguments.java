package org.zwobble.couscous.interpreter;

import java.util.List;

import org.zwobble.couscous.values.InterpreterValue;

import lombok.val;

public class Arguments {
    private int index = 0;
    private List<InterpreterValue> values;
    
    public Arguments(List<InterpreterValue> values) {
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    public <T extends InterpreterValue> T pop(Class<T> valueType) {
        val value = values.get(index);
        index++;
        if (valueType.isInstance(value)) {
            return (T) value;            
        } else {
            // TODO: throw a more specific error
            throw new UnsupportedOperationException();
        }
        
    }
}
