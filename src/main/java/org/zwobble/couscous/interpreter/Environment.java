package org.zwobble.couscous.interpreter;

import java.util.Map;
import java.util.Optional;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.interpreter.errors.UnboundVariable;
import org.zwobble.couscous.interpreter.errors.VariableNotInScope;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import static java.util.stream.Collectors.toMap;

public class Environment {
    private final Project project;
    private final Optional<InterpreterValue> thisValue;
    private final Map<Identifier, VariableEntry> stackFrame;
    
    public Environment(Project project, Optional<InterpreterValue> thisValue, Map<VariableDeclaration, Optional<InterpreterValue>> stackFrame) {
        this.project = project;
        this.thisValue = thisValue;
        this.stackFrame = stackFrame.entrySet().stream()
            .collect(toMap(
                entry -> entry.getKey().getId(),
                entry -> VariableEntry.of(entry.getKey().getType(), entry.getValue())));
    }
    
    public Optional<InterpreterValue> getThis() {
        return thisValue;
    }
    
    public InterpreterValue get(Identifier variableId) {
        checkVariableIsInScope(variableId);
        final org.zwobble.couscous.interpreter.Environment.VariableEntry entry = stackFrame.get(variableId);
        return entry.getValue().orElseThrow(() -> new UnboundVariable(variableId));
    }
    
    public void put(Identifier variableId, InterpreterValue value) {
        checkVariableIsInScope(variableId);
        checkVariableType(variableId, value);
        stackFrame.get(variableId).setValue(Optional.of(value));
    }
    
    public void put(VariableNode variable, InterpreterValue value) {
        put(variable.getDeclaration().getId(), value);
    }
    
    public ConcreteType findClass(TypeName className) {
        return project.findClass(className);
    }
    
    public Environment withStackFrame(Optional<InterpreterValue> thisValue, Map<VariableDeclaration, Optional<InterpreterValue>> stackFrame) {
        return new Environment(project, thisValue, stackFrame);
    }
    
    private void checkVariableIsInScope(Identifier variableId) {
        if (!stackFrame.containsKey(variableId)) {
            throw new VariableNotInScope(variableId);
        }
    }
    
    private void checkVariableType(Identifier variableId, InterpreterValue value) {
        TypeName variableType = stackFrame.get(variableId).getType();
        InterpreterTypes.checkIsInstance(variableType, value);
    }
    
    private static class VariableEntry {
        private final TypeName type;
        private Optional<InterpreterValue> value;
        
        private VariableEntry(final TypeName type, final Optional<InterpreterValue> value) {
            this.type = type;
            this.value = value;
        }
        
        public static VariableEntry of(final TypeName type, final Optional<InterpreterValue> value) {
            return new VariableEntry(type, value);
        }
        
        public TypeName getType() {
            return this.type;
        }
        
        public Optional<InterpreterValue> getValue() {
            return this.value;
        }
        
        public void setValue(final Optional<InterpreterValue> value) {
            this.value = value;
        }
    }
}