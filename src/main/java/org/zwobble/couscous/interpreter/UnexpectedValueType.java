package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.values.TypeReference;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class UnexpectedValueType extends InterpreterException {
    private static final long serialVersionUID = 1L;

    TypeReference expected;
    TypeReference actual;
}
