package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;

import lombok.Value;

@Value
public class Assignment implements ExpressionNode {
    VariableReferenceNode target;
    ExpressionNode value;
    
    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
