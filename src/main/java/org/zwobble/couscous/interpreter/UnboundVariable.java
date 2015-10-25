package org.zwobble.couscous.interpreter;

public final class UnboundVariable extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private final String variableId;
    
    public UnboundVariable(final String variableId) {
        this.variableId = variableId;
    }
    
    public String getVariableId() {
        return this.variableId;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "UnboundVariable(variableId=" + this.getVariableId() + ")";
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof UnboundVariable)) return false;
        final UnboundVariable other = (UnboundVariable)o;
        if (!other.canEqual((java.lang.Object)this)) return false;
        final java.lang.Object this$variableId = this.getVariableId();
        final java.lang.Object other$variableId = other.getVariableId();
        if (this$variableId == null ? other$variableId != null : !this$variableId.equals(other$variableId)) return false;
        return true;
    }
    
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof UnboundVariable;
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