package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class TryNode implements StatementNode {
    public static TryNode tryStatement(List<StatementNode> body, List<ExceptionHandlerNode> exceptionHandlers) {
        return new TryNode(body, exceptionHandlers);
    }

    private final List<StatementNode> body;
    private final List<ExceptionHandlerNode> exceptionHandlers;

    public TryNode(List<StatementNode> body, List<ExceptionHandlerNode> exceptionHandlers) {
        this.body = body;
        this.exceptionHandlers = exceptionHandlers;
    }

    public List<StatementNode> getBody() {
        return body;
    }

    public List<ExceptionHandlerNode> getExceptionHandlers() {
        return exceptionHandlers;
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
                transformer.transform(handler.getExceptionType()),
                transformer.transformStatements(handler.getBody()))));
    }

    @Override
    public String toString() {
        return "TryNode(" +
            "body=" + body +
            ", exceptionHandlers=" + exceptionHandlers +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TryNode tryNode = (TryNode) o;

        if (!body.equals(tryNode.body)) return false;
        return exceptionHandlers.equals(tryNode.exceptionHandlers);

    }

    @Override
    public int hashCode() {
        int result = body.hashCode();
        result = 31 * result + exceptionHandlers.hashCode();
        return result;
    }
}
