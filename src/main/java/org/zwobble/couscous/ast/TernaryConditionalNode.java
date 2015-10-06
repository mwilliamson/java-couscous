package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import lombok.Value;

@Value
public class TernaryConditionalNode implements ExpressionNode {
    ExpressionNode condition;
    ExpressionNode ifTrue;
    ExpressionNode ifFalse;
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public TypeName getType() {
        return ifTrue.getType();
    }
}
