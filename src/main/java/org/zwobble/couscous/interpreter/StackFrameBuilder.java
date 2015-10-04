package org.zwobble.couscous.interpreter;

import java.util.Map;

import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.values.InterpreterValue;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;

public class StackFrameBuilder {
    private final ImmutableMap.Builder<VariableNode, Optional<InterpreterValue>> variables
        = ImmutableMap.builder();
    
    public StackFrameBuilder declare(VariableNode variable, InterpreterValue value) {
        variables.put(variable, Optional.of(value));
        return this;
    }
    
    public StackFrameBuilder declare(VariableNode variable) {
        variables.put(variable, Optional.empty());
        return this;
    }
    
    public Map<VariableNode, Optional<InterpreterValue>> build() {
        return variables.build();
    }
}
