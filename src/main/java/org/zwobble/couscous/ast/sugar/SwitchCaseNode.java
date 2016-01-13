package org.zwobble.couscous.ast.sugar;

import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.StatementNode;

import java.util.List;
import java.util.Optional;

public class SwitchCaseNode {
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
}
