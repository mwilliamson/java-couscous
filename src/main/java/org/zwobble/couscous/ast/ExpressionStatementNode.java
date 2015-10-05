package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

import lombok.Value;

@Value
public class ExpressionStatementNode implements StatementNode {
    ExpressionNode expression;

    @Override
    public <T> T accept(StatementNodeMapper<T> visitor) {
        return visitor.visit(this);
    }
}
