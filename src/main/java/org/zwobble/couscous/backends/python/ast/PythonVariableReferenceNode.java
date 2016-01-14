package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonVariableReferenceNode implements PythonExpressionNode {
    private final String name;

    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }

    private PythonVariableReferenceNode(final String name) {
        this.name = name;
    }

    public static PythonVariableReferenceNode pythonVariableReference(final String name) {
        return new PythonVariableReferenceNode(name);
    }

    public String getName() {
        return this.name;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "PythonVariableReferenceNode(name=" + this.getName() + ")";
    }

    @Override
    public int precedence() {
        return Integer.MAX_VALUE;
    }
}