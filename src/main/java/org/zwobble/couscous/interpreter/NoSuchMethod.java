package org.zwobble.couscous.interpreter;

public final class NoSuchMethod extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private final String methodName;

    public NoSuchMethod(final String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "NoSuchMethod(methodName=" + this.getMethodName() + ")";
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof NoSuchMethod)) return false;
        final NoSuchMethod other = (NoSuchMethod)o;
        if (!other.canEqual((java.lang.Object)this)) return false;
        final java.lang.Object this$methodName = this.getMethodName();
        final java.lang.Object other$methodName = other.getMethodName();
        if (this$methodName == null ? other$methodName != null : !this$methodName.equals(other$methodName)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof NoSuchMethod;
    }

    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $methodName = this.getMethodName();
        result = result * PRIME + ($methodName == null ? 43 : $methodName.hashCode());
        return result;
    }
}