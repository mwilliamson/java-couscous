package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import static java.util.Arrays.asList;

import lombok.Value;

@Value(staticConstructor="staticMethodCall")
public class StaticMethodCallNode implements ExpressionNode {
    public static StaticMethodCallNode staticMethodCall(String className, String methodName, ExpressionNode... arguments) {
        return staticMethodCall(className, methodName, asList(arguments));
    }
    
    public static StaticMethodCallNode staticMethodCall(String className, String methodName, List<ExpressionNode> arguments) {
        return staticMethodCall(TypeName.of(className), methodName, arguments);
    }
    
    TypeName className;
    String methodName;
    List<ExpressionNode> arguments;
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public TypeName getType() {
        throw new UnsupportedOperationException();
    }
}
