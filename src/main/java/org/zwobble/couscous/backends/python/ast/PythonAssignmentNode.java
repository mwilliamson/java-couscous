package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonAssignmentNode implements PythonStatementNode {
    private final PythonExpressionNode target;
    private final PythonExpressionNode value;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonAssignmentNode(final PythonExpressionNode target, final PythonExpressionNode value) {
        this.target = target;
        this.value = value;
    }
    
    public static PythonAssignmentNode pythonAssignment(final PythonExpressionNode target, final PythonExpressionNode value) {
        return new PythonAssignmentNode(target, value);
    }
    
    public PythonExpressionNode getTarget() {
        return this.target;
    }
    
    public PythonExpressionNode getValue() {
        return this.value;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonAssignmentNode)) return false;
        final PythonAssignmentNode other = (PythonAssignmentNode)o;
        final java.lang.Object this$target = this.getTarget();
        final java.lang.Object other$target = other.getTarget();
        if (this$target == null ? other$target != null : !this$target.equals(other$target)) return false;
        final java.lang.Object this$value = this.getValue();
        final java.lang.Object other$value = other.getValue();
        if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $target = this.getTarget();
        result = result * PRIME + ($target == null ? 43 : $target.hashCode());
        final java.lang.Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonAssignmentNode(target=" + this.getTarget() + ", value=" + this.getValue() + ")";
    }
}