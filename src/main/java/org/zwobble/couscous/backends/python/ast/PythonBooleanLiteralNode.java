package org.zwobble.couscous.backends.python.ast;

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
    public java.lang.String toString() {
        return "PythonBooleanLiteralNode(value=" + this.getValue() + ")";
    }
}