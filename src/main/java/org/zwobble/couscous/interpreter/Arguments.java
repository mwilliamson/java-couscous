package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.interpreter.values.InterpreterValue;

import java.util.List;

public class Arguments {
    private List<InterpreterValue> values;
    
    public Arguments(List<InterpreterValue> values) {
        this.values = values;
    }
    
    public InterpreterValue get(int index) {
        return values.get(index);
    }

    public List<InterpreterValue> getValues() {
        return values;
    }
}
