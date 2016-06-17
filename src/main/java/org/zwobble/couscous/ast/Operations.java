package org.zwobble.couscous.ast;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.InternalCouscousValue;

import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.TypeCoercionNode.typeCoercion;
import static org.zwobble.couscous.util.ExtraLists.list;

public class Operations {
    public static ExpressionNode same(ExpressionNode left, ExpressionNode right) {
        return staticMethodCall(InternalCouscousValue.REF, "same", list(left, right), Types.BOOLEAN);
    }

    public static ExpressionNode not(ExpressionNode value) {
        if (value.getType().equals(Types.BOOLEAN)) {
            return OperationNode.operation(
                Operator.BOOLEAN_NOT, list(value),
                Types.BOOLEAN);
        } else {
            throw new IllegalArgumentException("Can only negate booleans");
        }
    }

    public static ExpressionNode booleanAnd(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.BOOLEAN_AND, left, right);
    }

    public static ExpressionNode booleanOr(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.BOOLEAN_OR, left, right);
    }

    public static ExpressionNode integerAdd(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.ADD, left, right);
    }

    public static ExpressionNode integerSubtract(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.SUBTRACT, left, right);
    }

    public static ExpressionNode integerMultiply(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.MULTIPLY, left, right);
    }

    public static ExpressionNode integerDivide(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.DIVIDE, left, right);
    }

    public static ExpressionNode integerMod(ExpressionNode left, ExpressionNode right) {
        return integerOperation(Operator.MOD, left, right);
    }

    public static ExpressionNode integerOperation(Operator operator, ExpressionNode left, ExpressionNode right) {
        return new OperationNode(operator, list(left, right), Types.INT);
    }

    public static ExpressionNode equal(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.EQUALS, left, right);
    }

    public static ExpressionNode notEqual(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.NOT_EQUALS, left, right);
    }

    public static ExpressionNode greaterThan(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.GREATER_THAN, left, right);
    }

    public static ExpressionNode greaterThanOrEqual(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.GREATER_THAN_OR_EQUAL, left, right);
    }

    public static ExpressionNode lessThan(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.LESS_THAN, left, right);
    }

    public static ExpressionNode lessThanOrEqual(ExpressionNode left, ExpressionNode right) {
        return booleanOperation(Operator.LESS_THAN_OR_EQUAL, left, right);
    }

    private static ExpressionNode booleanOperation(Operator operator, ExpressionNode left, ExpressionNode right) {
        return new OperationNode(operator, list(left, right), Types.BOOLEAN);
    }

    public static ExpressionNode boxInt(ExpressionNode value) {
        return typeCoercion(value, Types.BOXED_INT);
    }

    public static ExpressionNode unboxInt(ExpressionNode value) {
        return typeCoercion(value, Types.INT);
    }

    public static ExpressionNode instanceOf(ExpressionNode value, ScalarType type) {
        return booleanOperation(Operator.INSTANCE_OF, value, literal(type));
    }
}
