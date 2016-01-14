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
    public java.lang.String toString() {
        return "PythonStringLiteralNode(value=" + this.getValue() + ")";
    }

    @Override
    public int precedence() {
        return Integer.MAX_VALUE;
    }
}