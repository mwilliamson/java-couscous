package org.zwobble.couscous.interpreter.errors;

import org.zwobble.couscous.types.Type;

public class InvalidCast extends InterpreterException {
    private final Type expected;
    private final Type actual;

    public InvalidCast(Type expected, Type actual) {

        this.expected = expected;
        this.actual = actual;
    }

    public Type getExpected() {
        return expected;
    }

    public Type getActual() {
        return actual;
    }
}
