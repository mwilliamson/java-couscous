package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.util.ExtraIterables;

public class InstanceReceiver implements Receiver {
    public static Receiver instanceReceiver(ExpressionNode expression) {
        return new InstanceReceiver(expression);
    }

    private final ExpressionNode expression;

    public InstanceReceiver(ExpressionNode expression) {
        this.expression = expression;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(Mapper<T> visitor) {
        return visitor.visit(expression);
    }

    @Override
    public int type() {
        return NodeTypes.INSTANCE_RECEIVER;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(expression);
    }

    @Override
    public Receiver transformSubtree(NodeTransformer transformer) {
        return new InstanceReceiver(transformer.transformExpression(expression));
    }

    @Override
    public String toString() {
        return "InstanceReceiver(" +
            "expression=" + expression +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceReceiver that = (InstanceReceiver) o;

        return expression.equals(that.expression);

    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }
}
