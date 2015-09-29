package org.zwobble.couscous.interpreter;

import java.util.HashMap;
import java.util.Map;

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
}
