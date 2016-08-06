package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;

import java.util.List;

public class OperationNode implements ExpressionNode {
    public static OperationNode operation(Operator operator, List<ExpressionNode> arguments, Type type) {
        return new OperationNode(operator, arguments, type);
    }

    private final Operator operator;
    private final List<ExpressionNode> arguments;
    private Type type;

    OperationNode(Operator operator, List<ExpressionNode> arguments, Type type) {
        this.operator = operator;
        this.arguments = arguments;
        this.type = type;
    }

    public ExpressionNode desugar() {
        return MethodCallNode.methodCall(
            arguments.get(0),
            operator.getSymbol(),
            arguments.subList(1, arguments.size()),
            type);
    }

    public Operator getOperator() {
        return operator;
    }

    public OperatorType getOperatorType() {
        return operator.getType();
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int nodeType() {
        return NodeTypes.OPERATION;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return arguments;
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
        return new OperationNode(
            operator,
            transformer.transformExpressions(arguments),
            transformer.transform(type));
    }

    @Override
    public String toString() {
        return "OperationNode(" +
            "operator=" + operator +
            ", arguments=" + arguments +
            ", type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationNode that = (OperationNode) o;

        if (operator != that.operator) return false;
        if (!arguments.equals(that.arguments)) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = operator.hashCode();
        result = 31 * result + arguments.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
