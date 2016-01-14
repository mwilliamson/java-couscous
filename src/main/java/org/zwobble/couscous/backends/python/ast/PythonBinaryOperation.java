package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public class PythonBinaryOperation implements PythonExpressionNode {
    public static PythonExpressionNode pythonIs(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("is", 50, left, right);
    }

    public static PythonExpressionNode pythonAnd(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("and", 20, left, right);
    }

    public static PythonExpressionNode pythonOr(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("or", 20, left, right);
    }

    public static PythonExpressionNode pythonAdd(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("+", 100, left, right);
    }

    public static PythonExpressionNode pythonSubtract(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("-", 100, left, right);
    }

    public static PythonExpressionNode pythonMultiply(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("*", 110, left, right);
    }

    public static PythonExpressionNode pythonEqual(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("==", 50, left, right);
    }

    public static PythonExpressionNode pythonNotEqual(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("!=", 50, left, right);
    }

    public static PythonExpressionNode pythonLessThan(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("<", 50, left, right);
    }

    public static PythonExpressionNode pythonLessThanOrEqual(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation("<=", 50, left, right);
    }

    public static PythonExpressionNode pythonGreaterThan(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation(">", 50, left, right);
    }

    public static PythonExpressionNode pythonGreatThanOrEqual(PythonExpressionNode left, PythonExpressionNode right) {
        return new PythonBinaryOperation(">=", 50, left, right);
    }
    
    private final String operator;
    private final int precedence;
    private final PythonExpressionNode left;
    private final PythonExpressionNode right;

    private PythonBinaryOperation(
        String operator,
        int precedence,
        PythonExpressionNode left,
        PythonExpressionNode right)
    {
        this.operator = operator;
        this.precedence = precedence;
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

    @Override
    public int precedence() {
        return precedence;
    }
}
