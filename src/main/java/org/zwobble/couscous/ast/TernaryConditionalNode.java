package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.types.Type;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

public class TernaryConditionalNode implements ExpressionNode {
    public static TernaryConditionalNode ternaryConditional(
            ExpressionNode condition,
            ExpressionNode ifTrue,
            ExpressionNode ifFalse) {
        return new TernaryConditionalNode(condition, ifTrue, ifFalse);
    }
    
    private final ExpressionNode condition;
    private final ExpressionNode ifTrue;
    private final ExpressionNode ifFalse;
    
    private TernaryConditionalNode(
            ExpressionNode condition,
            ExpressionNode ifTrue,
            ExpressionNode ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }
    
    public ExpressionNode getCondition() {
        return condition;
    }
    
    public ExpressionNode getIfTrue() {
        return ifTrue;
    }
    
    public ExpressionNode getIfFalse() {
        return ifFalse;
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ExpressionNode transform(NodeTransformer transformer) {
        return new TernaryConditionalNode(
            transformer.transformExpression(condition),
            transformer.transformExpression(ifTrue),
            transformer.transformExpression(ifFalse));
    }

    @Override
    public Type getType() {
        return ifTrue.getType();
    }

    @Override
    public String toString() {
        return "TernaryConditionalNode(condition=" + condition + ", ifTrue="
               + ifTrue + ", ifFalse=" + ifFalse + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((ifFalse == null) ? 0 : ifFalse.hashCode());
        result = prime * result + ((ifTrue == null) ? 0 : ifTrue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TernaryConditionalNode other = (TernaryConditionalNode) obj;
        if (condition == null) {
            if (other.condition != null)
                return false;
        } else if (!condition.equals(other.condition))
            return false;
        if (ifFalse == null) {
            if (other.ifFalse != null)
                return false;
        } else if (!ifFalse.equals(other.ifFalse))
            return false;
        if (ifTrue == null) {
            if (other.ifTrue != null)
                return false;
        } else if (!ifTrue.equals(other.ifTrue))
            return false;
        return true;
    }
}
