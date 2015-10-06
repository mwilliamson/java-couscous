package org.zwobble.couscous.interpreter;

import java.util.List;

import org.zwobble.couscous.interpreter.values.InterpreterValue;

public class PositionalArguments {
    private List<InterpreterValue> values;
    
    public PositionalArguments(List<InterpreterValue> values) {
        this.values = values;
    }
    
    public InterpreterValue get(int index) {
        return values.get(index);
    }
}
