package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.util.ExtraIterables;

public class ThrowNode implements StatementNode {
    public static ThrowNode throwNode(ExpressionNode value) {
        return new ThrowNode(value);
    }

    private final ExpressionNode value;

    public ThrowNode(ExpressionNode value) {
        this.value = value;
    }

    public ExpressionNode getValue() {
        return value;
    }

    @Override
    public int nodeType() {
        return NodeTypes.THROW;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(value);
    }

    @Override
    public StatementNode transformSubtree(NodeTransformer transformer) {
        return new ThrowNode(transformer.transformExpression(value));
    }

    @Override
    public String toString() {
        return "ThrowNode(" +
            "value=" + value +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThrowNode throwNode = (ThrowNode) o;

        return value.equals(throwNode.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
