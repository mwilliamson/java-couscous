package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.AssignableExpressionNodeVisitor;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import lombok.Value;

@Value(staticConstructor="reference")
public class VariableReferenceNode implements AssignableExpressionNode {
    public static VariableReferenceNode reference(VariableNode variable) {
        return new VariableReferenceNode(variable.getDeclaration());
    }
    
    VariableDeclaration referent;

    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }
    
    public String getReferentId() {
        return referent.getId();
    }

    @Override
    public TypeName getType() {
        return referent.getType();
    }

    @Override
    public void accept(AssignableExpressionNodeVisitor mapper) {
        mapper.visit(this);
    }
}
