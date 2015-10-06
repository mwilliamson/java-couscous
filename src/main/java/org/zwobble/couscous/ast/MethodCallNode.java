package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import lombok.Value;

@Value
public class MethodCallNode implements ExpressionNode {
    ExpressionNode receiver;
    String methodName;
    List<ExpressionNode> arguments;
    TypeName type;
    
    public static MethodCallNode methodCall(
            ExpressionNode receiver,
            String methodName,
            List<ExpressionNode> arguments,
            TypeName type) {
        return new MethodCallNode(receiver, methodName, arguments, type);
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }
}
