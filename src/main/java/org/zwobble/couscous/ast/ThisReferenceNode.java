package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import lombok.Value;

@Value(staticConstructor="thisReference")
public class ThisReferenceNode implements ExpressionNode {
    TypeName type;

    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }
}
