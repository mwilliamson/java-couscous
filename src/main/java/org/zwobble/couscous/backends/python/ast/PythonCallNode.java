package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import java.util.List;

public final class PythonCallNode implements PythonExpressionNode {
    private final PythonExpressionNode callee;
    private final List<PythonExpressionNode> arguments;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonCallNode(final PythonExpressionNode callee, final List<PythonExpressionNode> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }
    
    public static PythonCallNode pythonCall(final PythonExpressionNode callee, final List<PythonExpressionNode> arguments) {
        return new PythonCallNode(callee, arguments);
    }
    
    public PythonExpressionNode getCallee() {
        return this.callee;
    }
    
    public List<PythonExpressionNode> getArguments() {
        return this.arguments;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonCallNode(callee=" + this.getCallee() + ", arguments=" + this.getArguments() + ")";
    }

    @Override
    public int precedence() {
        return 150;
    }
}