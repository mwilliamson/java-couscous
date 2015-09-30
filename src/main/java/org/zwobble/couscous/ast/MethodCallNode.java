package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;

import static java.util.Arrays.asList;

import lombok.Value;

@Value
public class MethodCallNode implements ExpressionNode {
    ExpressionNode receiver;
    String methodName;
    List<ExpressionNode> arguments;
    
    public static MethodCallNode methodCall(ExpressionNode receiver, String methodName, ExpressionNode... arguments) {
        return new MethodCallNode(receiver, methodName, asList(arguments));
    }
    
    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
