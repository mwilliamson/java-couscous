package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonStringLiteralNode implements PythonExpressionNode {
    private final String value;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonStringLiteralNode(final String value) {
        this.value = value;
    }
    
    public static PythonStringLiteralNode pythonStringLiteral(final String value) {
        return new PythonStringLiteralNode(value);
    }
    
    public String getValue() {
        return this.value;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonStringLiteralNode)) return false;
        final PythonStringLiteralNode other = (PythonStringLiteralNode)o;
        final java.lang.Object this$value = this.getValue();
        final java.lang.Object other$value = other.getValue();
        if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonStringLiteralNode(value=" + this.getValue() + ")";
    }
}