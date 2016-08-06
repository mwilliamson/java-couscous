package org.zwobble.couscous.ast;

import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.util.ExtraIterables;
import org.zwobble.couscous.util.ExtraLists;

import java.util.List;

public class ForNode implements StatementNode {
    private final List<LocalVariableDeclarationNode> initializers;
    private final ExpressionNode condition;
    private final List<ExpressionNode> updaters;
    private final List<StatementNode> statements;

    public ForNode(
        List<LocalVariableDeclarationNode> initializers,
        ExpressionNode condition,
        List<ExpressionNode> updaters,
        List<StatementNode> statements
    ) {
        this.initializers = initializers;
        this.condition = condition;
        this.updaters = updaters;
        this.statements = statements;
    }

    public List<LocalVariableDeclarationNode> getInitializers() {
        return initializers;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public List<ExpressionNode> getUpdaters() {
        return updaters;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public int nodeType() {
        return NodeTypes.FOR;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return Iterables.concat(
            initializers,
            ExtraIterables.of(condition),
            updaters,
            statements
        );
    }

    @Override
    public StatementNode transformSubtree(NodeTransformer transformer) {
        return new ForNode(
            ExtraLists.copyOf(ExtraIterables.cast(LocalVariableDeclarationNode.class, transformer.transformStatements(initializers))),
            transformer.transformExpression(condition),
            transformer.transformExpressions(updaters),
            transformer.transformStatements(statements)
        );
    }

    @Override
    public String toString() {
        return "ForNode(" +
            "initializers=" + initializers +
            ", condition=" + condition +
            ", updaters=" + updaters +
            ", statements=" + statements +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForNode forNode = (ForNode) o;

        if (!initializers.equals(forNode.initializers)) return false;
        if (!condition.equals(forNode.condition)) return false;
        if (!updaters.equals(forNode.updaters)) return false;
        return statements.equals(forNode.statements);

    }

    @Override
    public int hashCode() {
        int result = initializers.hashCode();
        result = 31 * result + condition.hashCode();
        result = 31 * result + updaters.hashCode();
        result = 31 * result + statements.hashCode();
        return result;
    }
}
