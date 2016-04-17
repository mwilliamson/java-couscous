package org.zwobble.couscous.ast;

import org.zwobble.couscous.types.Type;

import java.util.List;

public class ExceptionHandlerNode {
    public static ExceptionHandlerNode exceptionhandler(VariableDeclaration declaration, List<StatementNode> body) {
        return new ExceptionHandlerNode(declaration, body);
    }

    private final VariableDeclaration declaration;
    private final List<StatementNode> body;

    public ExceptionHandlerNode(VariableDeclaration declaration, List<StatementNode> body) {
        this.declaration = declaration;
        this.body = body;
    }

    public VariableDeclaration getDeclaration() {
        return declaration;
    }

    public List<StatementNode> getBody() {
        return body;
    }

    public Type getExceptionType() {
        return declaration.getType();
    }

    public String getExceptionName() {
        return declaration.getName();
    }

    @Override
    public String toString() {
        return "ExceptionHandlerNode(" +
            "declaration=" + declaration +
            ", body=" + body +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExceptionHandlerNode that = (ExceptionHandlerNode) o;

        if (!declaration.equals(that.declaration)) return false;
        return body.equals(that.body);

    }

    @Override
    public int hashCode() {
        int result = declaration.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }
}
