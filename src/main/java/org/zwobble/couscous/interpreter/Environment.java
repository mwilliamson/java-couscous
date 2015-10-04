package org.zwobble.couscous.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.couscous.Project;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.values.ConcreteType;
import org.zwobble.couscous.values.InterpreterValue;

public class Environment {
    private final Map<Integer, InterpreterValue> stackFrame;
    private final Project project;

    public Environment(Project project, Map<Integer, InterpreterValue> stackFrame) {
        this.project = project;
        this.stackFrame = new HashMap<>(stackFrame);
    }

    public InterpreterValue get(int referentId) {
        return stackFrame.get(referentId);
    }

    public void put(int variableId, InterpreterValue value) {
        stackFrame.put(variableId, value);
    }

    public void put(VariableNode variable, InterpreterValue value) {
        stackFrame.put(variable.getId(), value);
    }

    public ConcreteType<?> findClass(String className) {
        // TODO: handle missing classes
        return project.findClass(className);
    }

    public Environment withStackFrame(Map<Integer, InterpreterValue> stackFrame) {
        return new Environment(project, stackFrame);
    }
}
