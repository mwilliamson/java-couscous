package org.zwobble.couscous.backends.python.ast;

import javax.annotation.Nullable;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonBooleanLiteralNode implements PythonExpressionNode {
    private final boolean value;
    
    public boolean getValue() {
        return value;
    }
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonBooleanLiteralNode(final boolean value) {
        this.value = value;
    }
    
    public static PythonBooleanLiteralNode pythonBooleanLiteral(final boolean value) {
        return new PythonBooleanLiteralNode(value);
    }
    
    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonBooleanLiteralNode)) return false;
        final PythonBooleanLiteralNode other = (PythonBooleanLiteralNode)o;
        if (this.getValue() != other.getValue()) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.getValue() ? 79 : 97);
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonBooleanLiteralNode(value=" + this.getValue() + ")";
    }
}