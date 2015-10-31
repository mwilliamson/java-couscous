package org.zwobble.couscous.interpreter;

import javax.annotation.Nullable;

import org.zwobble.couscous.interpreter.values.InterpreterValue;

public final class ConditionMustBeBoolean extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private final InterpreterValue actual;

    public ConditionMustBeBoolean(final InterpreterValue actual) {
        this.actual = actual;
    }

    public InterpreterValue getActual() {
        return this.actual;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "ConditionMustBeBoolean(actual=" + this.getActual() + ")";
    }

    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ConditionMustBeBoolean)) return false;
        final ConditionMustBeBoolean other = (ConditionMustBeBoolean)o;
        if (!other.canEqual((java.lang.Object)this)) return false;
        final java.lang.Object this$actual = this.getActual();
        final java.lang.Object other$actual = other.getActual();
        if (this$actual == null ? other$actual != null : !this$actual.equals(other$actual)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof ConditionMustBeBoolean;
    }

    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $actual = this.getActual();
        result = result * PRIME + ($actual == null ? 43 : $actual.hashCode());
        return result;
    }
}