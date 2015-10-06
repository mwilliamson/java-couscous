package org.zwobble.couscous.interpreter;

import java.util.Map;
import java.util.Optional;

import org.zwobble.couscous.Project;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import org.zwobble.couscous.interpreter.values.InterpreterValue;

import static java.util.stream.Collectors.toMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class Environment {
    private final Project project;
    private final Map<Integer, VariableEntry> stackFrame;

    public Environment(Project project, Map<VariableDeclaration, Optional<InterpreterValue>> stackFrame) {
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
        put(variable.getDeclaration().getId(), value);
    }

    public ConcreteType<?> findClass(TypeName className) {
        // TODO: handle missing classes
        return project.findClass(className);
    }

    public Environment withStackFrame(Map<VariableDeclaration, Optional<InterpreterValue>> stackFrame) {
        return new Environment(project, stackFrame);
    }

    private void checkVariableIsInScope(int variableId) {
        if (!stackFrame.containsKey(variableId)) {
            throw new VariableNotInScope(variableId);
        }
    }

    private void checkVariableType(int variableId, InterpreterValue value) {
        val variableType = stackFrame.get(variableId).getType();
        val valueType = value.getType().getName();
        if (!variableType.equals(valueType)) {
            throw new UnexpectedValueType(variableType, valueType);
        }
    }
    
    @AllArgsConstructor(staticName="of")
    @Getter
    @Setter
    private static class VariableEntry {
        private final TypeName type;
        private Optional<InterpreterValue> value;
    }
}
