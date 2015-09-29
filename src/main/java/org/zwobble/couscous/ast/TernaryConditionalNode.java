package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;

import lombok.Value;

@Value
public class TernaryConditionalNode implements ExpressionNode {
    ExpressionNode condition;
    ExpressionNode ifTrue;
    ExpressionNode ifFalse;
    
    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
