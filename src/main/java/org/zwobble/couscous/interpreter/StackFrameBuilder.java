package org.zwobble.couscous.interpreter;

import java.util.Map;

import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.values.InterpreterValue;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;

public class StackFrameBuilder {
    private final ImmutableMap.Builder<VariableDeclaration, Optional<InterpreterValue>> variables
        = ImmutableMap.builder();
    
    public StackFrameBuilder declare(VariableNode variable, InterpreterValue value) {
        variables.put(variable.getDeclaration(), Optional.of(value));
        return this;
    }
    
    public StackFrameBuilder declare(VariableNode variable) {
        variables.put(variable.getDeclaration(), Optional.empty());
        return this;
    }
    
    public Map<VariableDeclaration, Optional<InterpreterValue>> build() {
        return variables.build();
    }
}
