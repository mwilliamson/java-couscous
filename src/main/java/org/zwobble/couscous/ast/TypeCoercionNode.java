package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.ast.types.Type;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

public class TypeCoercionNode implements ExpressionNode {
    public static TypeCoercionNode typeCoercion(ExpressionNode expression, Type type) {
        return new TypeCoercionNode(expression, type);
    }

    private final ExpressionNode expression;
    private final Type type;

    private TypeCoercionNode(ExpressionNode expression, Type type) {
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
    public ExpressionNode transform(NodeTransformer transformer) {
        return new TypeCoercionNode(
            transformer.transformExpression(expression),
            transformer.transform(type));
    }

    @Override
    public String toString() {
        return "TypeCoercionNode(" +
            "expression=" + expression +
            ", type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeCoercionNode that = (TypeCoercionNode) o;

        if (!expression.equals(that.expression)) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
