package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;

public class StatementBlockNode implements StatementNode {
    private final List<StatementNode> statements;

    public StatementBlockNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public int nodeType() {
        return NodeTypes.STATEMENT_BLOCK;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return statements;
    }

    @Override
    public StatementNode transformSubtree(NodeTransformer transformer) {
        return new StatementBlockNode(transformer.transformStatements(statements));
    }

    @Override
    public String toString() {
        return "StatementBlockNode(" +
            "statements=" + statements +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatementBlockNode that = (StatementBlockNode) o;

        return statements.equals(that.statements);

    }

    @Override
    public int hashCode() {
        return statements.hashCode();
    }
}
