package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class MethodCallNode implements ExpressionNode {
    public static ExpressionNode not(ExpressionNode value) {
        if (value.getType().equals(BooleanValue.REF)) {
            return methodCall(value, "negate", Collections.emptyList(), BooleanValue.REF);
        } else {
            throw new IllegalArgumentException("Can only negate booleans");
        }
    }

    public static ExpressionNode integerAdd(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.ADD.getMethodName(), left, right);
    }

    public static ExpressionNode integerSubtract(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.SUBTRACT.getMethodName(), left, right);
    }

    public static ExpressionNode integerMultiply(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.MULTIPLY.getMethodName(), left, right);
    }

    public static ExpressionNode integerDivide(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.DIVIDE.getMethodName(), left, right);
    }

    public static ExpressionNode integerMod(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.MOD.getMethodName(), left, right);
    }

    public static MethodCallNode integerOperation(String methodName, ExpressionNode left, ExpressionNode right) {
        return methodCall(left, methodName, asList(right), IntegerValue.REF);
    }

    public static ExpressionNode equal(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.EQUALS.getMethodName(), left, right);
    }

    public static ExpressionNode notEqual(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.NOT_EQUALS.getMethodName(), left, right);
    }

    public static ExpressionNode greaterThan(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.GREATER_THAN.getMethodName(), left, right);
    }

    public static ExpressionNode greaterThanOrEqual(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.GREATER_THAN_OR_EQUAL.getMethodName(), left, right);
    }

    public static ExpressionNode lessThan(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.LESS_THAN.getMethodName(), left, right);
    }

    public static ExpressionNode lessThanOrEqual(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.LESS_THAN_OR_EQUAL.getMethodName(), left, right);
    }

    private static ExpressionNode booleanOperation(String methodName, ExpressionNode left, ExpressionNode right) {
        return methodCall(left, methodName, asList(right), BooleanValue.REF);
    }

    public static MethodCallNode methodCall(
            ExpressionNode receiver,
            String methodName,
            List<ExpressionNode> arguments,
            TypeName type) {
        return new MethodCallNode(receiver, methodName, arguments, type);
    }
    
    private final ExpressionNode receiver;
    private final String methodName;
    private final List<ExpressionNode> arguments;
    private final TypeName type;
    
    public MethodCallNode(
            ExpressionNode receiver,
            String methodName,
            List<ExpressionNode> arguments,
            TypeName type) {
        this.receiver = receiver;
        this.methodName = methodName;
        this.arguments = arguments;
        this.type = type;
    }
    
    public ExpressionNode getReceiver() {
        return receiver;
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
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ExpressionNode replaceExpressions(Function<ExpressionNode, ExpressionNode> replace) {
        return new MethodCallNode(
            replace.apply(receiver),
            methodName,
            eagerMap(arguments, replace::apply),
            type);
    }

    @Override
    public String toString() {
        return "MethodCallNode(receiver=" + receiver + ", methodName="
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
                 + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result
                 + ((receiver == null) ? 0 : receiver.hashCode());
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
        MethodCallNode other = (MethodCallNode) obj;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        if (receiver == null) {
            if (other.receiver != null)
                return false;
        } else if (!receiver.equals(other.receiver))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
