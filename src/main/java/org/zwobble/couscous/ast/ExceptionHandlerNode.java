package org.zwobble.couscous.ast;

import org.zwobble.couscous.types.Type;

import java.util.List;

public class ExceptionHandlerNode {
    public static ExceptionHandlerNode exceptionhandler(Type type, List<StatementNode> body) {
        return new ExceptionHandlerNode(type, body);
    }

    private final Type type;
    private final List<StatementNode> body;

    public ExceptionHandlerNode(Type type, List<StatementNode> body) {
        this.type = type;
        this.body = body;
    }

    public Type getExceptionType() {
        return type;
    }

    public List<StatementNode> getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ExceptionHandlerNode(" +
            "type=" + type +
            ", body=" + body +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExceptionHandlerNode that = (ExceptionHandlerNode) o;

        if (!type.equals(that.type)) return false;
        return body.equals(that.body);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }
}
