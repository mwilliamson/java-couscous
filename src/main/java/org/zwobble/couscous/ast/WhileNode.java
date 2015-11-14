package org.zwobble.couscous.ast;

import com.google.common.base.Objects;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

import java.util.List;

public class WhileNode implements StatementNode {
    public static WhileNode whileLoop(ExpressionNode condition, List<StatementNode> body) {
        return new WhileNode(condition, body);
    }

    private final ExpressionNode condition;
    private final List<StatementNode> body;

    public WhileNode(ExpressionNode condition, List<StatementNode> body) {
        this.condition = condition;
        this.body = body;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public List<StatementNode> getBody() {
        return body;
    }

    @Override
    public <T> T accept(StatementNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhileNode whileNode = (WhileNode) o;
        return Objects.equal(condition, whileNode.condition) &&
            Objects.equal(body, whileNode.body);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(condition, body);
    }

    @Override
    public String toString() {
        return "WhileNode(" +
            "condition=" + condition +
            ", body=" + body +
            ')';
    }
}
