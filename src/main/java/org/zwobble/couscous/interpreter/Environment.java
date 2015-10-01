package org.zwobble.couscous.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.couscous.values.ConcreteType;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.InterpreterValue;

public class Environment {
    private Map<Integer, InterpreterValue> stackFrame;

    public Environment(Map<Integer, InterpreterValue> stackFrame) {
        this.stackFrame = new HashMap<>(stackFrame);
    }

    public InterpreterValue get(int referentId) {
        return stackFrame.get(referentId);
    }

    public void put(int referentId, InterpreterValue value) {
        stackFrame.put(referentId, value);
    }

    public ConcreteType<?> findClass(String className) {
        if (className == "java.lang.Integer") {
            return IntegerValue.TYPE;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
