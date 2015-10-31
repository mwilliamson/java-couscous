package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonReturnNode implements PythonStatementNode {
    private final PythonExpressionNode value;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonReturnNode(final PythonExpressionNode value) {
        this.value = value;
    }
    
    public static PythonReturnNode pythonReturn(final PythonExpressionNode value) {
        return new PythonReturnNode(value);
    }
    
    public PythonExpressionNode getValue() {
        return this.value;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonReturnNode(value=" + this.getValue() + ")";
    }
}