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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonVariableReferenceNode)) return false;
        final PythonVariableReferenceNode other = (PythonVariableReferenceNode)o;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        return true;
    }

    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "PythonVariableReferenceNode(name=" + this.getName() + ")";
    }
}