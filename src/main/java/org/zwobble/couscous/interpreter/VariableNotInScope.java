package org.zwobble.couscous.interpreter;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class VariableNotInScope extends InterpreterException {
    private static final long serialVersionUID = 1L;
    
    int variableId;
}
