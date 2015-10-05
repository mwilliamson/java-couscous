package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.interpreter.values.InterpreterValue;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class ConditionMustBeBoolean extends InterpreterException {
    private static final long serialVersionUID = 1L;

    InterpreterValue actual;
}
