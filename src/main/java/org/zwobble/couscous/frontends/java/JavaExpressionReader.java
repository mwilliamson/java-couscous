package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.StaticMethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.frontends.java.JavaOperators.readOperator;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;

public class JavaExpressionReader {
    static ReadResult<ExpressionNode> readExpression(TypeName targetType, Expression expression) {
        return readExpressionWithoutBoxing(expression)
            .map(couscousExpression -> handleBoxing(targetType, couscousExpression));
    }

    private static ExpressionNode handleBoxing(TypeName targetType, ExpressionNode couscousExpression) {
        if (isIntegerBox(targetType, couscousExpression)) {
            return StaticMethodCallNode.boxInt(couscousExpression);
        } else if (isIntegerUnbox(targetType, couscousExpression)) {
            return StaticMethodCallNode.unboxInt(couscousExpression);
        } else if (isBooleanBox(targetType, couscousExpression)) {
            return StaticMethodCallNode.boxBoolean(couscousExpression);
        } else if (isBooleanUnbox(targetType, couscousExpression)) {
            return StaticMethodCallNode.unboxBoolean(couscousExpression);
        } else {
            return couscousExpression;
        }
    }

    private static TypeName unboxedType(TypeName type) {
        if (type.equals(ObjectValues.BOXED_INT)) {
            return IntegerValue.REF;
        } else if (type.equals(ObjectValues.BOXED_BOOLEAN)) {
            return BooleanValue.REF;
        } else {
            return type;
        }
    }

    private static boolean isIntegerBox(TypeName targetType, ExpressionNode expression) {
        return expression.getType().equals(IntegerValue.REF) &&
            !targetType.equals(IntegerValue.REF);
    }

    private static boolean isIntegerUnbox(TypeName targetType, ExpressionNode expression) {
        return !expression.getType().equals(IntegerValue.REF) &&
            targetType.equals(IntegerValue.REF);
    }

    private static boolean isBooleanBox(TypeName targetType, ExpressionNode expression) {
        return expression.getType().equals(BooleanValue.REF) &&
            !targetType.equals(BooleanValue.REF);
    }

    private static boolean isBooleanUnbox(TypeName targetType, ExpressionNode expression) {
        return !expression.getType().equals(BooleanValue.REF) &&
            targetType.equals(BooleanValue.REF);
    }

    static ReadResult<ExpressionNode> readExpressionWithoutBoxing(Expression expression) {
        switch (expression.getNodeType()) {
            case ASTNode.BOOLEAN_LITERAL:
                return ReadResult.of(readBooleanLiteral((BooleanLiteral)expression));

            case ASTNode.NUMBER_LITERAL:
                return ReadResult.of(readNumberLiteral((NumberLiteral)expression));

            case ASTNode.STRING_LITERAL:
                return ReadResult.of(readStringLiteral((StringLiteral)expression));

            case ASTNode.SIMPLE_NAME:
                return ReadResult.of(readSimpleName((SimpleName)expression));

            case ASTNode.THIS_EXPRESSION:
                return ReadResult.of(readThisExpression((ThisExpression)expression));

            case ASTNode.FIELD_ACCESS:
                return readFieldAccess((FieldAccess)expression);

            case ASTNode.METHOD_INVOCATION:
                return readMethodInvocation((MethodInvocation)expression);

            case ASTNode.CLASS_INSTANCE_CREATION:
                return readClassInstanceCreation((ClassInstanceCreation)expression);

            case ASTNode.INFIX_EXPRESSION:
                return readInfixExpression((InfixExpression)expression);

            case ASTNode.PREFIX_EXPRESSION:
                return readPrefixExpression((PrefixExpression)expression);

            case ASTNode.CONDITIONAL_EXPRESSION:
                return readConditionalExpression((ConditionalExpression)expression);

            case ASTNode.ASSIGNMENT:
                return readAssignment((Assignment)expression);

            default:
                throw new RuntimeException("Unsupported expression: " + expression.getClass());

        }
    }

    private static ExpressionNode readBooleanLiteral(BooleanLiteral expression) {
        return literal(expression.booleanValue());
    }

    private static LiteralNode readNumberLiteral(NumberLiteral expression) {
        return literal(Integer.parseInt(expression.getToken()));
    }

    private static ExpressionNode readStringLiteral(StringLiteral expression) {
        return literal(expression.getLiteralValue());
    }

    private static ExpressionNode readSimpleName(SimpleName expression) {
        IBinding binding = expression.resolveBinding();
        if (binding.getKind() == IBinding.VARIABLE) {
            return readVariableBinding((IVariableBinding)binding);
        } else {
            throw new RuntimeException("Unsupported binding: " + binding.getClass());
        }
    }

    private static ExpressionNode readVariableBinding(IVariableBinding binding) {
        TypeName type = typeOf(binding);
        if (binding.getDeclaringClass() == null) {
            return reference(var(binding.getKey(), binding.getName(), type));
        } else {
            return FieldAccessNode.fieldAccess(ThisReferenceNode.thisReference(typeOf(binding.getDeclaringClass())), binding.getName(), type);
        }
    }

    private static ExpressionNode readThisExpression(ThisExpression expression) {
        return ThisReferenceNode.thisReference(typeOf(expression));
    }

    private static ReadResult<ExpressionNode> readFieldAccess(FieldAccess expression) {
        return readExpressionWithoutBoxing(expression.getExpression())
            .map(left -> FieldAccessNode.fieldAccess(left, expression.getName().getIdentifier(), typeOf(expression)));
    }

    private static ReadResult<ExpressionNode> readMethodInvocation(MethodInvocation expression) {
        String methodName = expression.getName().getIdentifier();
        final TypeName type = typeOf(expression);
        IMethodBinding methodBinding = expression.resolveMethodBinding();
        TypeName receiverType = typeOf(methodBinding.getDeclaringClass());

        @SuppressWarnings("unchecked")
        ReadResult<List<ExpressionNode>> argumentsResult = readArguments(
            expression.resolveMethodBinding(),
            expression.arguments());

        return argumentsResult.flatMap(arguments -> {
            if ((methodBinding.getModifiers() & Modifier.STATIC) != 0) {
                return ReadResult.of(staticMethodCall(
                    receiverType,
                    methodName,
                    arguments,
                    type));
            } else {
                ReadResult<ExpressionNode> receiverResult = expression.getExpression() == null
                    ? ReadResult.of(ThisReferenceNode.thisReference(receiverType))
                    : readExpressionWithoutBoxing(expression.getExpression());

                return receiverResult.map(receiver -> MethodCallNode.methodCall(
                    receiver,
                    methodName,
                    arguments,
                    type));
            }
        });
    }

    private static ReadResult<ExpressionNode> readClassInstanceCreation(ClassInstanceCreation expression) {
        @SuppressWarnings("unchecked")
        List<Expression> javaArguments = expression.arguments();
        return readArguments(expression.resolveConstructorBinding(), javaArguments)
            .map(arguments -> constructorCall(typeOf(expression), arguments));
    }

    private static ReadResult<List<ExpressionNode>> readArguments(IMethodBinding method, List<Expression> javaArguments) {
        Stream<ReadResult<ExpressionNode>> results = IntStream.range(0, javaArguments.size())
            .mapToObj(index -> readExpression(
                typeOf(method.getParameterTypes()[index]),
                javaArguments.get(index)));
        return ReadResult.combine(results);
    }

    private static ReadResult<ExpressionNode> readInfixExpression(InfixExpression expression) {
        if (isPrimitiveOperation(expression)) {
            Operator operator = readOperator(expression.getOperator());
            return readPrimitiveOperation(
                operator,
                expression.getLeftOperand(),
                expression.getRightOperand());
        } else {
            return ReadResult.map(
                readExpression(ObjectValues.OBJECT, expression.getLeftOperand()),
                readExpression(ObjectValues.OBJECT, expression.getRightOperand()),
                (left, right) -> {
                    if (expression.getOperator() == InfixExpression.Operator.EQUALS) {
                        return StaticMethodCallNode.same(left, right);
                    } else if (expression.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
                        return MethodCallNode.not(StaticMethodCallNode.same(left, right));
                    } else {
                        throw new IllegalArgumentException("Unsupported operator: " + expression.getOperator());
                    }
                });
        }
    }

    private static boolean isPrimitiveOperation(InfixExpression expression) {
        TypeName leftOperandType = typeOf(expression.getLeftOperand());
        TypeName rightOperandType = typeOf(expression.getRightOperand());
        return isPrimitive(leftOperandType)
            || isPrimitive(rightOperandType)
            || (expression.getOperator() != InfixExpression.Operator.NOT_EQUALS
            && expression.getOperator() != InfixExpression.Operator.EQUALS);
    }

    private static boolean isPrimitive(TypeName type) {
        return type.equals(BooleanValue.REF) || type.equals(IntegerValue.REF);
    }

    private static ReadResult<ExpressionNode> readPrefixExpression(PrefixExpression expression) {
        Operator operator = readOperator(expression.getOperator());
        return readExpression(IntegerValue.REF, expression.getOperand())
            .map(operand -> AssignmentNode.assign(
                (AssignableExpressionNode) readExpressionWithoutBoxing(expression.getOperand()),
                MethodCallNode.methodCall(
                    operand,
                    operator.getMethodName(),
                    asList(literal(1)),
                    IntegerValue.REF)));
    }

    private static ReadResult<ExpressionNode> readConditionalExpression(ConditionalExpression expression) {
        TypeName type = typeOf(expression);
        return ReadResult.map(
            readExpression(BooleanValue.REF, expression.getExpression()),
            readExpression(type, expression.getThenExpression()),
            readExpression(type, expression.getElseExpression()),
            TernaryConditionalNode::ternaryConditional);
    }

    private static ReadResult<ExpressionNode> readAssignment(Assignment expression) {
        ReadResult<ExpressionNode> leftResult = readExpressionWithoutBoxing(expression.getLeftHandSide());
        if (expression.getOperator() == Assignment.Operator.ASSIGN) {
            return ReadResult.map(
                leftResult,
                readExpression(leftResult.getValue().getType(), expression.getRightHandSide()),
                AssignmentNode::assign);
        } else {
            Operator operator = readOperator(expression.getOperator());
            return ReadResult.map(
                leftResult,
                readPrimitiveOperation(operator, expression.getLeftHandSide(), expression.getRightHandSide()),
                (left, right) -> AssignmentNode.assign(
                    left,
                    handleBoxing(
                        leftResult.getValue().getType(),
                        right)));
        }
    }

    private static ReadResult<ExpressionNode> readPrimitiveOperation(Operator operator, Expression leftJava, Expression rightJava) {
        return ReadResult.map(
            readUnboxedExpression(leftJava),
            readUnboxedExpression(rightJava),
            (left, right) -> MethodCallNode.methodCall(
                left,
                operator.getMethodName(),
                asList(right),
                operator.isBoolean() ? BooleanValue.REF : left.getType()));
    }

    private static ReadResult<ExpressionNode> readUnboxedExpression(Expression expression) {
        return readExpression(
            unboxedType(typeOf(expression)),
            expression);
    }
}
