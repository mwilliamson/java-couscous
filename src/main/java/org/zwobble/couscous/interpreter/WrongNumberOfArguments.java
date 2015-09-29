package org.zwobble.couscous.interpreter;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class WrongNumberOfArguments extends InterpreterException {
    private static final long serialVersionUID = 1L;
    
    int expected;
    int actual;
}
