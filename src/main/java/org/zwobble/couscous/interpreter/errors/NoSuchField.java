package org.zwobble.couscous.interpreter.errors;

public final class NoSuchField extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private final String fieldName;

    public NoSuchField(final String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public java.lang.String toString() {
        return "NoSuchField(fieldName=" + this.getFieldName() + ")";
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof NoSuchField)) return false;
        final NoSuchField other = (NoSuchField)o;
        if (!other.canEqual((java.lang.Object)this)) return false;
        final java.lang.Object this$fieldName = this.getFieldName();
        final java.lang.Object other$fieldName = other.getFieldName();
        if (this$fieldName == null ? other$fieldName != null : !this$fieldName.equals(other$fieldName)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof NoSuchField;
    }

    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $fieldName = this.getFieldName();
        result = result * PRIME + ($fieldName == null ? 43 : $fieldName.hashCode());
        return result;
    }
}