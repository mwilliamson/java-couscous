package org.zwobble.couscous.ast.sugar;

import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.NodeTypes;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.util.ExtraIterables;

import java.util.List;
import java.util.Optional;

public class SwitchCaseNode implements Node {
    public static SwitchCaseNode switchCase(ExpressionNode value, List<StatementNode> statements) {
        return switchCase(Optional.of(value), statements);
    }

    public static SwitchCaseNode switchCase(List<StatementNode> statements) {
        return switchCase(Optional.empty(), statements);
    }

    public static SwitchCaseNode switchCase(Optional<ExpressionNode> value, List<StatementNode> statements) {
        return new SwitchCaseNode(value, statements);
    }

    private final Optional<ExpressionNode> value;
    private final List<StatementNode> statements;

    public SwitchCaseNode(Optional<ExpressionNode> value, List<StatementNode> statements) {
        this.value = value;
        this.statements = statements;
    }

    public boolean isDefault() {
        return !value.isPresent();
    }

    public Optional<ExpressionNode> getValue() {
        return value;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public int nodeType() {
        return NodeTypes.SWITCH_CASE;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return Iterables.concat(
            ExtraIterables.iterable(value),
            statements
        );
    }

    @Override
    public SwitchCaseNode transformSubtree(NodeTransformer transformer) {
        return new SwitchCaseNode(
            value.map(transformer::transformExpression),
            transformer.transformStatements(statements)
        );
    }
}
