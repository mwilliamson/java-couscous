package org.zwobble.couscous.ast;

import java.util.List;
import java.util.function.Function;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.InternalCouscousValue;
import org.zwobble.couscous.values.ObjectValues;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.TypeCoercionNode.typeCoercion;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class StaticMethodCallNode implements ExpressionNode {
    public static ExpressionNode same(ExpressionNode left, ExpressionNode right) {
        return staticMethodCall(InternalCouscousValue.REF, "same", asList(left, right), BooleanValue.REF);
    }
    
    public static ExpressionNode boxInt(ExpressionNode value) {
        return typeCoercion(value, ObjectValues.BOXED_INT);
    }

    public static ExpressionNode unboxInt(ExpressionNode value) {
        return typeCoercion(value, IntegerValue.REF);
    }
    
    public static ExpressionNode boxBoolean(ExpressionNode value) {
        return typeCoercion(value, ObjectValues.BOXED_BOOLEAN);
    }

    public static ExpressionNode unboxBoolean(ExpressionNode value) {
        return typeCoercion(value, BooleanValue.REF);
    }
    
    public static StaticMethodCallNode staticMethodCall(
            String className,
            String methodName,
            List<ExpressionNode> arguments,
            TypeName type) {
        return staticMethodCall(TypeName.of(className), methodName, arguments, type);
    }
    
    public static StaticMethodCallNode staticMethodCall(
            TypeName className,
            String methodName,
            List<ExpressionNode> arguments,
            TypeName type) {
        return new StaticMethodCallNode(className, methodName, arguments, type);
    }
    
    private final TypeName className;
    private final String methodName;
    private final List<ExpressionNode> arguments;
    private final TypeName type;
    
    public StaticMethodCallNode(
            TypeName className,
            String methodName,
            List<ExpressionNode> arguments,
            TypeName type) {
        this.className = className;
        this.methodName = methodName;
        this.arguments = arguments;
        this.type = type;
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
    
    public TypeName getType() {
        return type;
    }

    public MethodSignature signature() {
        return new MethodSignature(methodName, eagerMap(arguments, argument -> argument.getType()));
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ExpressionNode replaceExpressions(Function<ExpressionNode, ExpressionNode> replace) {
        return new StaticMethodCallNode(
            className,
            methodName,
            eagerMap(arguments, replace::apply),
            type);
    }

    @Override
    public String toString() {
        return "StaticMethodCallNode(className=" + className + ", methodName="
               + methodName + ", arguments=" + arguments + ", type=" + type
               + ")";
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
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
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
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
