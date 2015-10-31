package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import javax.annotation.Nullable;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonGetSliceNode implements PythonExpressionNode {
    private final PythonExpressionNode receiver;
    private final List<PythonExpressionNode> arguments;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonGetSliceNode(final PythonExpressionNode receiver, final List<PythonExpressionNode> arguments) {
        this.receiver = receiver;
        this.arguments = arguments;
    }
    
    public static PythonGetSliceNode pythonGetSlice(final PythonExpressionNode receiver, final List<PythonExpressionNode> arguments) {
        return new PythonGetSliceNode(receiver, arguments);
    }
    
    public PythonExpressionNode getReceiver() {
        return this.receiver;
    }
    
    public List<PythonExpressionNode> getArguments() {
        return this.arguments;
    }
    
    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonGetSliceNode)) return false;
        final PythonGetSliceNode other = (PythonGetSliceNode)o;
        final java.lang.Object this$receiver = this.getReceiver();
        final java.lang.Object other$receiver = other.getReceiver();
        if (this$receiver == null ? other$receiver != null : !this$receiver.equals(other$receiver)) return false;
        final java.lang.Object this$arguments = this.getArguments();
        final java.lang.Object other$arguments = other.getArguments();
        if (this$arguments == null ? other$arguments != null : !this$arguments.equals(other$arguments)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $receiver = this.getReceiver();
        result = result * PRIME + ($receiver == null ? 43 : $receiver.hashCode());
        final java.lang.Object $arguments = this.getArguments();
        result = result * PRIME + ($arguments == null ? 43 : $arguments.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonGetSliceNode(receiver=" + this.getReceiver() + ", arguments=" + this.getArguments() + ")";
    }
}