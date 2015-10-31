package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public class PythonNotNode implements PythonExpressionNode {
    public static PythonExpressionNode pythonNot(PythonExpressionNode operand) {
        return new PythonNotNode(operand);
    }
    
    private final PythonExpressionNode operand;

    private PythonNotNode(PythonExpressionNode operand) {
        this.operand = operand;
    }
    
    public PythonExpressionNode getOperand() {
        return operand;
    }
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
