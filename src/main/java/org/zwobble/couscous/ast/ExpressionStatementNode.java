package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;

import lombok.Value;

@Value
public class ExpressionStatementNode implements StatementNode {
    ExpressionNode expression;

    @Override
    public <T> T accept(StatementNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
