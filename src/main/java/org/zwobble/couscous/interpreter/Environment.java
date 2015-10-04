package org.zwobble.couscous.interpreter;

import java.util.Map;
import java.util.Optional;

import org.zwobble.couscous.Project;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.values.ConcreteType;
import org.zwobble.couscous.values.InterpreterValue;
import org.zwobble.couscous.values.TypeReference;

import static java.util.stream.Collectors.toMap;
import static org.zwobble.couscous.util.CousCousMaps.mapKeys;

import lombok.val;

public class Environment {
    private final Project project;
    private final Map<Integer, Optional<InterpreterValue>> stackFrameValues;
    private final Map<Integer, TypeReference> stackFrameTypes;

    public Environment(Project project, Map<VariableNode, Optional<InterpreterValue>> stackFrame) {
        this.project = project;
        this.stackFrameValues = mapKeys(stackFrame, variable -> variable.getId());
        this.stackFrameTypes = stackFrame.keySet()
            .stream()
            .collect(toMap(variable -> variable.getId(), variable -> variable.getType()));
    }

    public InterpreterValue get(int variableId) {
        checkVariableIsInScope(variableId);
        val value = stackFrameValues.get(variableId);
        return value.orElseThrow(() -> new UnboundVariable(variableId));
    }

    public void put(int variableId, InterpreterValue value) {
        checkVariableIsInScope(variableId);
        checkVariableType(variableId, value);
        stackFrameValues.put(variableId, Optional.of(value));
    }

    public void put(VariableNode variable, InterpreterValue value) {
        put(variable.getId(), value);
    }

    public ConcreteType<?> findClass(String className) {
        // TODO: handle missing classes
        return project.findClass(className);
    }

    public Environment withStackFrame(Map<VariableNode, Optional<InterpreterValue>> stackFrame) {
        return new Environment(project, stackFrame);
    }

    private void checkVariableIsInScope(int variableId) {
        if (!stackFrameValues.containsKey(variableId)) {
            throw new VariableNotInScope(variableId);
        }
    }

    private void checkVariableType(int variableId, InterpreterValue value) {
        val variableType = stackFrameTypes.get(variableId);
        val valueType = value.getType().getReference();
        if (!variableType.equals(valueType)) {
            throw new UnexpectedValueType(variableType, valueType);
        }
    }
}
