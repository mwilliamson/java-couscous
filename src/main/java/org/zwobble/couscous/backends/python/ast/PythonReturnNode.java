package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonReturnNode implements PythonStatementNode {
    private final PythonExpressionNode value;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonReturnNode(final PythonExpressionNode value) {
        this.value = value;
    }
    
    public static PythonReturnNode pythonReturn(final PythonExpressionNode value) {
        return new PythonReturnNode(value);
    }
    
    public PythonExpressionNode getValue() {
        return this.value;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonReturnNode)) return false;
        final PythonReturnNode other = (PythonReturnNode)o;
        final java.lang.Object this$value = this.getValue();
        final java.lang.Object other$value = other.getValue();
        if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonReturnNode(value=" + this.getValue() + ")";
    }
}