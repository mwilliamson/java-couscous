package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;

public class OperationNode implements ExpressionNode {
    public static OperationNode operation(Operator operator, List<ExpressionNode> arguments, TypeName type) {
        return new OperationNode(operator, arguments, type);
    }

    private final Operator operator;
    private final List<ExpressionNode> arguments;
    private TypeName type;

    OperationNode(Operator operator, List<ExpressionNode> arguments, TypeName type) {
        this.operator = operator;
        this.arguments = arguments;
        this.type = type;
    }

    public ExpressionNode desugar() {
        return MethodCallNode.methodCall(
            arguments.get(0),
            operator.getMethodName(),
            arguments.subList(1, arguments.size()),
            type);
    }

    public Operator getOperator() {
        return operator;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
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
    public ExpressionNode transform(NodeTransformer transformer) {
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
