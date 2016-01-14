package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonIntegerLiteralNode implements PythonExpressionNode {
    private final int value;

    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }

    private PythonIntegerLiteralNode(final int value) {
        this.value = value;
    }

    public static PythonIntegerLiteralNode pythonIntegerLiteral(final int value) {
        return new PythonIntegerLiteralNode(value);
    }

    public int getValue() {
        return this.value;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "PythonIntegerLiteralNode(value=" + this.getValue() + ")";
    }

    @Override
    public int precedence() {
        return Integer.MAX_VALUE;
    }
}