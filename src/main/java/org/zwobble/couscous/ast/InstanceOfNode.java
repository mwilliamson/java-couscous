package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.util.ExtraIterables;

public class InstanceOfNode implements ExpressionNode {
    private final ExpressionNode left;
    private final ScalarType right;

    public InstanceOfNode(ExpressionNode left, ScalarType right) {
        this.left = left;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ScalarType getRight() {
        return right;
    }

    @Override
    public Type getType() {
        return Types.BOOLEAN;
    }

    @Override
    public int nodeType() {
        return NodeTypes.INSTANCE_OF;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(left);
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
        return new InstanceOfNode(
            transformer.transformExpression(left),
            transformer.transform(right));
    }

    @Override
    public String toString() {
        return "InstanceOfNode(" +
            "left=" + left +
            ", right=" + right +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceOfNode that = (InstanceOfNode) o;

        if (!left.equals(that.left)) return false;
        return right.equals(that.right);

    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }
}
