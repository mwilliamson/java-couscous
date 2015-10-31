package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import javax.annotation.Nullable;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

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
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonCallNode)) return false;
        final PythonCallNode other = (PythonCallNode)o;
        final java.lang.Object this$callee = this.getCallee();
        final java.lang.Object other$callee = other.getCallee();
        if (this$callee == null ? other$callee != null : !this$callee.equals(other$callee)) return false;
        final java.lang.Object this$arguments = this.getArguments();
        final java.lang.Object other$arguments = other.getArguments();
        if (this$arguments == null ? other$arguments != null : !this$arguments.equals(other$arguments)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $callee = this.getCallee();
        result = result * PRIME + ($callee == null ? 43 : $callee.hashCode());
        final java.lang.Object $arguments = this.getArguments();
        result = result * PRIME + ($arguments == null ? 43 : $arguments.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonCallNode(callee=" + this.getCallee() + ", arguments=" + this.getArguments() + ")";
    }
}