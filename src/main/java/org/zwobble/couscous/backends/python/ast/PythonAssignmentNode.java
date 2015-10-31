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
    public java.lang.String toString() {
        return "PythonAssignmentNode(target=" + this.getTarget() + ", value=" + this.getValue() + ")";
    }
}