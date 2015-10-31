package org.zwobble.couscous.backends.python.ast;

import javax.annotation.Nullable;

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
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonConditionalExpressionNode)) return false;
        final PythonConditionalExpressionNode other = (PythonConditionalExpressionNode)o;
        final java.lang.Object this$condition = this.getCondition();
        final java.lang.Object other$condition = other.getCondition();
        if (this$condition == null ? other$condition != null : !this$condition.equals(other$condition)) return false;
        final java.lang.Object this$trueValue = this.getTrueValue();
        final java.lang.Object other$trueValue = other.getTrueValue();
        if (this$trueValue == null ? other$trueValue != null : !this$trueValue.equals(other$trueValue)) return false;
        final java.lang.Object this$falseValue = this.getFalseValue();
        final java.lang.Object other$falseValue = other.getFalseValue();
        if (this$falseValue == null ? other$falseValue != null : !this$falseValue.equals(other$falseValue)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $condition = this.getCondition();
        result = result * PRIME + ($condition == null ? 43 : $condition.hashCode());
        final java.lang.Object $trueValue = this.getTrueValue();
        result = result * PRIME + ($trueValue == null ? 43 : $trueValue.hashCode());
        final java.lang.Object $falseValue = this.getFalseValue();
        result = result * PRIME + ($falseValue == null ? 43 : $falseValue.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonConditionalExpressionNode(condition=" + this.getCondition() + ", trueValue=" + this.getTrueValue() + ", falseValue=" + this.getFalseValue() + ")";
    }
}