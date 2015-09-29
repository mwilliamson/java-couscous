package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;

import static org.zwobble.couscous.ast.VariableReferenceNode.reference;

import lombok.Value;

@Value
public class Assignment implements ExpressionNode {
    public static Assignment assign(VariableNode target, ExpressionNode value) {
        return new Assignment(reference(target), value);
    }
    
    VariableReferenceNode target;
    ExpressionNode value;
    
    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
