package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

import static org.zwobble.couscous.types.Types.concrete;

public class TypeCoercionNode implements ExpressionNode {
    public static ExpressionNode coerce(ExpressionNode expression, Type type) {
        if (concrete(type).equals(concrete(expression.getType()))) {
            return expression;
        } else {
            return new TypeCoercionNode(expression, type);
        }
    }

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
    public int nodeType() {
        return NodeTypes.TYPE_COERCION;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(expression);
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
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
