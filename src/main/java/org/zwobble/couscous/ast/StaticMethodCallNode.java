package org.zwobble.couscous.ast;

import java.util.List;

import javax.annotation.Nullable;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import static java.util.Arrays.asList;

public class StaticMethodCallNode implements ExpressionNode {
    public static StaticMethodCallNode staticMethodCall(String className, String methodName, ExpressionNode... arguments) {
        return staticMethodCall(className, methodName, asList(arguments));
    }
    
    public static StaticMethodCallNode staticMethodCall(String className, String methodName, List<ExpressionNode> arguments) {
        return staticMethodCall(TypeName.of(className), methodName, arguments);
    }
    
    public static StaticMethodCallNode staticMethodCall(
            TypeName className,
            String methodName,
            List<ExpressionNode> arguments) {
        return new StaticMethodCallNode(className, methodName, arguments);
    }
    
    private final TypeName className;
    private final String methodName;
    private final List<ExpressionNode> arguments;
    
    public StaticMethodCallNode(
            TypeName className,
            String methodName,
            List<ExpressionNode> arguments) {
        this.className = className;
        this.methodName = methodName;
        this.arguments = arguments;
    }
    
    public TypeName getClassName() {
        return className;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public List<ExpressionNode> getArguments() {
        return arguments;
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public TypeName getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "StaticMethodCallNode(className=" + className + ", methodName="
               + methodName + ", arguments=" + arguments + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result
                 + ((className == null) ? 0 : className.hashCode());
        result = prime * result
                 + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StaticMethodCallNode other = (StaticMethodCallNode) obj;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }
}
