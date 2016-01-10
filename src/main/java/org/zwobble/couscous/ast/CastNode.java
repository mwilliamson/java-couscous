package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import java.util.function.Function;

public class CastNode implements ExpressionNode {
    public static ExpressionNode cast(ExpressionNode expression, TypeName type) {
        return new CastNode(expression, type);
    }

    private final ExpressionNode expression;
    private final TypeName type;

    private CastNode(ExpressionNode expression, TypeName type) {
        this.expression = expression;
        this.type = type;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public TypeName getType() {
        return type;
    }

    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ExpressionNode replaceExpressions(Function<ExpressionNode, ExpressionNode> replace) {
        return new CastNode(replace.apply(expression), type);
    }

    @Override
    public String toString() {
        return "CastNode(" +
            "expression=" + expression +
            ", type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CastNode castNode = (CastNode) o;

        if (!expression.equals(castNode.expression)) return false;
        return type.equals(castNode.type);

    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
