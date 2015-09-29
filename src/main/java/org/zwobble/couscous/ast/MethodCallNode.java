package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;

import lombok.Value;

@Value
public class MethodCallNode implements ExpressionNode {
    ExpressionNode receiver;
    String methodName;
    
    public static MethodCallNode methodCall(ExpressionNode receiver, String methodName) {
        return new MethodCallNode(receiver, methodName);
    }
    
    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
