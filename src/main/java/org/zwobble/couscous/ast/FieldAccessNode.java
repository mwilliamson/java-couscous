package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.AssignableExpressionNodeVisitor;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import lombok.Value;

@Value(staticConstructor="fieldAccess")
public class FieldAccessNode implements AssignableExpressionNode {
    ExpressionNode left;
    String fieldName;
    TypeName type;
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(AssignableExpressionNodeVisitor mapper) {
        mapper.visit(this);
    }
}
