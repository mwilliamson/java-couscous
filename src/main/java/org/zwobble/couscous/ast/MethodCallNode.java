package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;
import org.zwobble.couscous.values.TypeReference;

import lombok.Value;

@Value
public class MethodCallNode implements ExpressionNode {
    ExpressionNode receiver;
    String methodName;
    List<ExpressionNode> arguments;
    TypeReference type;
    
    public static MethodCallNode methodCall(
            ExpressionNode receiver,
            String methodName,
            List<ExpressionNode> arguments,
            TypeReference type) {
        return new MethodCallNode(receiver, methodName, arguments, type);
    }
    
    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
