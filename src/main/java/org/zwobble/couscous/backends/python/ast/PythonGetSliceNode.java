package org.zwobble.couscous.backends.python.ast;

import java.util.List;
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
    public java.lang.String toString() {
        return "PythonGetSliceNode(receiver=" + this.getReceiver() + ", arguments=" + this.getArguments() + ")";
    }
}