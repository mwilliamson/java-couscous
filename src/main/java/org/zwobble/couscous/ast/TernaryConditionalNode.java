package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.values.TypeReference;

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
    public TypeReference getType() {
        return ifTrue.getType();
    }
}
