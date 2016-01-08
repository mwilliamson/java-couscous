package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public class PythonBinaryOperation implements PythonExpressionNode {
    public static PythonExpressionNode pythonIs(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("is", left, right);
    }

    public static PythonExpressionNode pythonAdd(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("+", left, right);
    }
    
    private final String operator;
    private final PythonExpressionNode left;
    private final PythonExpressionNode right;

    public PythonBinaryOperation(
            String operator,
            PythonExpressionNode left,
            PythonExpressionNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public PythonExpressionNode getLeft() {
        return left;
    }
    
    public PythonExpressionNode getRight() {
        return right;
    }

    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
