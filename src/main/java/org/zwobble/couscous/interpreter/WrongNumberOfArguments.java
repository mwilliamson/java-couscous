package org.zwobble.couscous.interpreter;

import javax.annotation.Nullable;

public final class WrongNumberOfArguments extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private final int expected;
    private final int actual;
    
    public WrongNumberOfArguments(final int expected, final int actual) {
        this.expected = expected;
        this.actual = actual;
    }
    
    public int getExpected() {
        return this.expected;
    }
    
    public int getActual() {
        return this.actual;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "WrongNumberOfArguments(expected=" + this.getExpected() + ", actual=" + this.getActual() + ")";
    }
    
    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof WrongNumberOfArguments)) return false;
        final WrongNumberOfArguments other = (WrongNumberOfArguments)o;
        if (!other.canEqual((java.lang.Object)this)) return false;
        if (this.getExpected() != other.getExpected()) return false;
        if (this.getActual() != other.getActual()) return false;
        return true;
    }
    
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof WrongNumberOfArguments;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getExpected();
        result = result * PRIME + this.getActual();
        return result;
    }
}