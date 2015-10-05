package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;

import lombok.Value;

@Value
public class VariableReferenceNode implements ExpressionNode {
    public static VariableReferenceNode reference(VariableNode variable) {
        return new VariableReferenceNode(variable.getDeclaration());
    }
    
    VariableDeclaration referent;

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
    public int getReferentId() {
        return referent.getId();
    }
}
