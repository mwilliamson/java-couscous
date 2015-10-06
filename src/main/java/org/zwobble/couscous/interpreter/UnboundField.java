package org.zwobble.couscous.interpreter;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class UnboundField extends InterpreterException {
    private static final long serialVersionUID = 1L;

    String fieldName;
}
