package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonConditionalExpressionNode implements PythonExpressionNode {
    private final PythonExpressionNode condition;
    private final PythonExpressionNode trueValue;
    private final PythonExpressionNode falseValue;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonConditionalExpressionNode(final PythonExpressionNode condition, final PythonExpressionNode trueValue, final PythonExpressionNode falseValue) {
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }
    
    public static PythonConditionalExpressionNode pythonConditionalExpression(final PythonExpressionNode condition, final PythonExpressionNode trueValue, final PythonExpressionNode falseValue) {
        return new PythonConditionalExpressionNode(condition, trueValue, falseValue);
    }
    
    public PythonExpressionNode getCondition() {
        return this.condition;
    }
    
    public PythonExpressionNode getTrueValue() {
        return this.trueValue;
    }
    
    public PythonExpressionNode getFalseValue() {
        return this.falseValue;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonConditionalExpressionNode(condition=" + this.getCondition() + ", trueValue=" + this.getTrueValue() + ", falseValue=" + this.getFalseValue() + ")";
    }

    @Override
    public int precedence() {
        return 10;
    }
}