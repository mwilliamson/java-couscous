package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import lombok.Value;

@Value(staticConstructor="constructorCall")
public class ConstructorCallNode implements ExpressionNode {
    TypeName type;
    List<ExpressionNode> arguments;
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }
}
