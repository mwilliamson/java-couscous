package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

import lombok.Value;

@Value
public class ReturnNode implements StatementNode {
    ExpressionNode value;

    @Override
    public <T> T accept(StatementNodeMapper<T> visitor) {
        return visitor.visit(this);
    }
}
