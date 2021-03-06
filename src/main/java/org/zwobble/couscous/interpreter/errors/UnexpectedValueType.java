package org.zwobble.couscous.interpreter.errors;

import org.zwobble.couscous.types.Type;

public final class UnexpectedValueType extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private final Type expected;
    private final Type actual;

    public UnexpectedValueType(final Type expected, final Type actual) {
        this.expected = expected;
        this.actual = actual;
    }

    public Type getExpected() {
        return this.expected;
    }

    public Type getActual() {
        return this.actual;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "UnexpectedValueType(expected=" + this.getExpected() + ", actual=" + this.getActual() + ")";
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof UnexpectedValueType)) return false;
        final UnexpectedValueType other = (UnexpectedValueType)o;
        if (!other.canEqual((java.lang.Object)this)) return false;
        final java.lang.Object this$expected = this.getExpected();
        final java.lang.Object other$expected = other.getExpected();
        if (this$expected == null ? other$expected != null : !this$expected.equals(other$expected)) return false;
        final java.lang.Object this$actual = this.getActual();
        final java.lang.Object other$actual = other.getActual();
        if (this$actual == null ? other$actual != null : !this$actual.equals(other$actual)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof UnexpectedValueType;
    }

    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $expected = this.getExpected();
        result = result * PRIME + ($expected == null ? 43 : $expected.hashCode());
        final java.lang.Object $actual = this.getActual();
        result = result * PRIME + ($actual == null ? 43 : $actual.hashCode());
        return result;
    }
}