package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;

import static java.util.Arrays.asList;

import lombok.Value;

@Value
public class StaticMethodCallNode implements ExpressionNode {
    public static StaticMethodCallNode staticMethodCall(String className, String methodName, ExpressionNode... arguments) {
        return new StaticMethodCallNode(className, methodName, asList(arguments));
    }
    
    String className;
    String methodName;
    List<ExpressionNode> arguments;
    
    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
