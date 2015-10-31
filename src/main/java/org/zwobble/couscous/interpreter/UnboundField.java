package org.zwobble.couscous.interpreter;

import javax.annotation.Nullable;

public final class UnboundField extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private final String fieldName;

    public UnboundField(final String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "UnboundField(fieldName=" + this.getFieldName() + ")";
    }

    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof UnboundField)) return false;
        final UnboundField other = (UnboundField)o;
        if (!other.canEqual((java.lang.Object)this)) return false;
        final java.lang.Object this$fieldName = this.getFieldName();
        final java.lang.Object other$fieldName = other.getFieldName();
        if (this$fieldName == null ? other$fieldName != null : !this$fieldName.equals(other$fieldName)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof UnboundField;
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