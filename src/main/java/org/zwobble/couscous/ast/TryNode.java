package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class TryNode implements StatementNode {
    public static TryNode tryStatement(
        List<StatementNode> body,
        List<ExceptionHandlerNode> exceptionHandlers,
        List<StatementNode> finallyBody)
    {
        return new TryNode(body, exceptionHandlers, finallyBody);
    }

    private final List<StatementNode> body;
    private final List<ExceptionHandlerNode> exceptionHandlers;
    private final List<StatementNode> finallyBody;

    public TryNode(
        List<StatementNode> body,
        List<ExceptionHandlerNode> exceptionHandlers,
        List<StatementNode> finallyBody)
    {
        this.body = body;
        this.exceptionHandlers = exceptionHandlers;
        this.finallyBody = finallyBody;
    }

    public List<StatementNode> getBody() {
        return body;
    }

    public List<ExceptionHandlerNode> getExceptionHandlers() {
        return exceptionHandlers;
    }

    public List<StatementNode> getFinallyBody() {
        return finallyBody;
    }

    @Override
    public <T> T accept(StatementNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public StatementNode transform(NodeTransformer transformer) {
        return new TryNode(
            transformer.transformStatements(body),
            eagerMap(exceptionHandlers, handler -> new ExceptionHandlerNode(
                transformer.transform(handler.getDeclaration()),
                transformer.transformStatements(handler.getBody()))), finallyBody);
    }

    @Override
    public String toString() {
        return "TryNode(" +
            "body=" + body +
            ", exceptionHandlers=" + exceptionHandlers +
            ", finallyBody=" + finallyBody +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TryNode tryNode = (TryNode) o;

        if (!body.equals(tryNode.body)) return false;
        if (!exceptionHandlers.equals(tryNode.exceptionHandlers)) return false;
        return finallyBody.equals(tryNode.finallyBody);
    }

    @Override
    public int hashCode() {
        int result = body.hashCode();
        result = 31 * result + exceptionHandlers.hashCode();
        result = 31 * result + finallyBody.hashCode();
        return result;
    }
}
