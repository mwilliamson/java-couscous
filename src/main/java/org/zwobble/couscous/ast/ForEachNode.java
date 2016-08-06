package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.util.ExtraIterables;

import java.util.List;

public class ForEachNode implements StatementNode {
    private final VariableDeclaration target;
    private final ExpressionNode iterable;
    private final List<StatementNode> statements;

    public ForEachNode(VariableDeclaration target, ExpressionNode iterable, List<StatementNode> statements) {
        this.target = target;
        this.iterable = iterable;
        this.statements = statements;
    }

    public VariableDeclaration getTarget() {
        return target;
    }

    public ExpressionNode getIterable() {
        return iterable;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public int nodeType() {
        return NodeTypes.FOR_EACH;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(iterable);
    }

    @Override
    public StatementNode transformSubtree(NodeTransformer transformer) {
        return new ForEachNode(
            transformer.transform(target),
            transformer.transformExpression(iterable),
            transformer.transformStatements(statements)
        );
    }

    @Override
    public String toString() {
        return "ForEachNode(" +
            "target=" + target +
            ", iterable=" + iterable +
            ", statements=" + statements +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForEachNode that = (ForEachNode) o;

        if (!target.equals(that.target)) return false;
        if (!iterable.equals(that.iterable)) return false;
        return statements.equals(that.statements);

    }

    @Override
    public int hashCode() {
        int result = target.hashCode();
        result = 31 * result + iterable.hashCode();
        result = 31 * result + statements.hashCode();
        return result;
    }
}
