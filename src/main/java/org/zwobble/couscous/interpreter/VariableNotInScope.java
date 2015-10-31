package org.zwobble.couscous.interpreter;

import javax.annotation.Nullable;

public final class VariableNotInScope extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private final String variableId;
    
    public VariableNotInScope(final String variableId) {
        this.variableId = variableId;
    }
    
    public String getVariableId() {
        return this.variableId;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "VariableNotInScope(variableId=" + this.getVariableId() + ")";
    }
    
    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof VariableNotInScope)) return false;
        final VariableNotInScope other = (VariableNotInScope)o;
        if (!other.canEqual((java.lang.Object)this)) return false;
        final java.lang.Object this$variableId = this.getVariableId();
        final java.lang.Object other$variableId = other.getVariableId();
        if (this$variableId == null ? other$variableId != null : !this$variableId.equals(other$variableId)) return false;
        return true;
    }
    
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof VariableNotInScope;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $variableId = this.getVariableId();
        result = result * PRIME + ($variableId == null ? 43 : $variableId.hashCode());
        return result;
    }
}