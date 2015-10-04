package org.zwobble.couscous.interpreter;

import java.util.Map;
import java.util.Optional;

import org.zwobble.couscous.Project;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.values.ConcreteType;
import org.zwobble.couscous.values.InterpreterValue;
import org.zwobble.couscous.values.TypeReference;

import static java.util.stream.Collectors.toMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class Environment {
    private final Project project;
    private final Map<Integer, VariableEntry> stackFrame;

    public Environment(Project project, Map<VariableNode, Optional<InterpreterValue>> stackFrame) {
        this.project = project;
        this.stackFrame = stackFrame.entrySet()
            .stream()
            .collect(toMap(
                entry -> entry.getKey().getId(),
                entry -> VariableEntry.of(entry.getKey().getType(), entry.getValue())));
    }

    public InterpreterValue get(int variableId) {
        checkVariableIsInScope(variableId);
        val entry = stackFrame.get(variableId);
        return entry.getValue().orElseThrow(() -> new UnboundVariable(variableId));
    }

    public void put(int variableId, InterpreterValue value) {
        checkVariableIsInScope(variableId);
        checkVariableType(variableId, value);
        stackFrame.get(variableId).setValue(Optional.of(value));
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
        if (!stackFrame.containsKey(variableId)) {
            throw new VariableNotInScope(variableId);
        }
    }

    private void checkVariableType(int variableId, InterpreterValue value) {
        val variableType = stackFrame.get(variableId).getType();
        val valueType = value.getType().getReference();
        if (!variableType.equals(valueType)) {
            throw new UnexpectedValueType(variableType, valueType);
        }
    }
    
    @AllArgsConstructor(staticName="of")
    @Getter
    @Setter
    private static class VariableEntry {
        private final TypeReference type;
        private Optional<InterpreterValue> value;
    }
}
