package org.zwobble.couscous.ast;

import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.util.ExtraIterables;

public class CastNode implements ExpressionNode {
    public static ExpressionNode cast(ExpressionNode expression, Type type) {
        return new CastNode(expression, type);
    }

    private final ExpressionNode expression;
    private final Type type;

    private CastNode(ExpressionNode expression, Type type) {
        this.expression = expression;
        this.type = type;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(expression);
    }

    @Override
    public ExpressionNode transform(NodeTransformer transformer) {
        return new CastNode(transformer.transformExpression(expression), transformer.transform(type));
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
