package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;

import lombok.Value;

@Value
public class ReturnNode implements StatementNode {
    ExpressionNode value;

    @Override
    public <T> T accept(StatementNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
