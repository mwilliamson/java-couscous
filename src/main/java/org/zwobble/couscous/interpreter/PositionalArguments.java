package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.interpreter.values.InterpreterValue;

import java.util.Iterator;
import java.util.List;

public class PositionalArguments implements Iterable<InterpreterValue> {
    private List<InterpreterValue> values;
    
    public PositionalArguments(List<InterpreterValue> values) {
        this.values = values;
    }
    
    public InterpreterValue get(int index) {
        return values.get(index);
    }

    @Override
    public Iterator<InterpreterValue> iterator() {
        return values.iterator();
    }
}
