package org.zwobble.couscous.tests.ast.visitors;

import org.junit.Test;
import org.zwobble.couscous.ast.OperationNode;
import org.zwobble.couscous.ast.Operations;
import org.zwobble.couscous.ast.Operator;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.OperationNode.operation;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.util.ExtraLists.list;

public class NodeTransformerTests {
    @Test
    public void subNodesAreTransformedBeforeExpression() {
        NodeTransformer transformer = NodeTransformer.builder()
            .transformExpression(node -> {
                if (node.equals(literal(1))) {
                    return Optional.of(literal(2));
                } else if (node instanceof OperationNode) {
                    return Optional.of(operation(Operator.ADD, ((OperationNode) node).getArguments(), node.getType()));
                } else {
                    return Optional.empty();
                }
            })
            .build();

        assertEquals(
            Operations.integerAdd(literal(2), literal(3)),
            transformer.transformExpression(Operations.integerMultiply(literal(1), literal(3)))
        );
    }
    @Test
    public void subNodesAreTransformedBeforeStatement() {
        NodeTransformer transformer = NodeTransformer.builder()
            .transformExpression(node -> Optional.of(literal(1)))
            .transformStatement(node -> {
                if (node instanceof ReturnNode) {
                    return Optional.of(list(expressionStatement(((ReturnNode) node).getValue())));
                } else {
                    return Optional.empty();
                }
            })
            .build();

        assertEquals(
            list(expressionStatement(literal(1))),
            transformer.transformStatement(returns(literal(0)))
        );
    }
}
