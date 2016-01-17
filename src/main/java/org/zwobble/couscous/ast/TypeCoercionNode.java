package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.function.Function;

public class TypeCoercionNode implements ExpressionNode {
    public static TypeCoercionNode typeCoercion(ExpressionNode expression, TypeName type) {
        return new TypeCoercionNode(expression, type);
    }

    private final ExpressionNode expression;
    private final TypeName type;

    private TypeCoercionNode(ExpressionNode expression, TypeName type) {
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
        return new TypeCoercionNode(replace.apply(expression), type);
    }

    public ExpressionNode transform(NodeTransformer transformer) {
        return new TypeCoercionNode(
            transformer.visit(expression),
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
