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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonIntegerLiteralNode)) return false;
        final PythonIntegerLiteralNode other = (PythonIntegerLiteralNode)o;
        if (this.getValue() != other.getValue()) return false;
        return true;
    }

    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getValue();
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "PythonIntegerLiteralNode(value=" + this.getValue() + ")";
    }
}