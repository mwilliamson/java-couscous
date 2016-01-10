package org.zwobble.couscous.interpreter.errors;

import org.zwobble.couscous.ast.TypeName;

public class InvalidCast extends RuntimeException {
    private final TypeName expected;
    private final TypeName actual;

    public InvalidCast(TypeName expected, TypeName actual) {

        this.expected = expected;
        this.actual = actual;
    }

    public TypeName getExpected() {
        return expected;
    }

    public TypeName getActual() {
        return actual;
    }
}
