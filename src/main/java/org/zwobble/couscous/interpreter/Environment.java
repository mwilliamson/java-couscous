package org.zwobble.couscous.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.zwobble.couscous.Project;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.values.ConcreteType;
import org.zwobble.couscous.values.InterpreterValue;

import lombok.val;

public class Environment {
    private final Map<Integer, Optional<InterpreterValue>> stackFrame;
    private final Project project;

    public Environment(Project project, Map<Integer, Optional<InterpreterValue>> stackFrame) {
        this.project = project;
        this.stackFrame = new HashMap<>(stackFrame);
    }

    public InterpreterValue get(int variableId) {
        checkVariableIsInScope(variableId);
        val value = stackFrame.get(variableId);
        return value.orElseThrow(() -> new UnboundVariable(variableId));
    }

    public void put(int variableId, InterpreterValue value) {
        checkVariableIsInScope(variableId);
        stackFrame.put(variableId, Optional.of(value));
    }

    public void put(VariableNode variable, InterpreterValue value) {
        put(variable.getId(), value);
    }

    public ConcreteType<?> findClass(String className) {
        // TODO: handle missing classes
        return project.findClass(className);
    }

    public Environment withStackFrame(Map<Integer, Optional<InterpreterValue>> stackFrame) {
        return new Environment(project, stackFrame);
    }

    private void checkVariableIsInScope(int variableId) {
        if (!stackFrame.containsKey(variableId)) {
            throw new VariableNotInScope(variableId);
        }
    }
}
