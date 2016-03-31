package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.types.Type;

import java.util.List;

public class Arguments {
    private final List<Type> types;
    private final List<InterpreterValue> values;
    
    public Arguments(List<Type> types, List<InterpreterValue> values) {
        this.types = types;
        this.values = values;
    }
    
    public InterpreterValue get(int index) {
        return values.get(index);
    }

    public List<Type> getTypes() {
        return types;
    }

    public List<InterpreterValue> getValues() {
        return values;
    }
}
