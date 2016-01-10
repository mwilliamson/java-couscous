package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.zwobble.couscous.ast.CastNode.cast;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.Operations.not;
import static org.zwobble.couscous.ast.TypeCoercionNode.typeCoercion;
import static org.zwobble.couscous.frontends.java.JavaOperators.readOperator;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.concat;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaExpressionReader {
    private final Scope scope;
    private final JavaReader javaReader;

    JavaExpressionReader(Scope scope, JavaReader javaReader) {
        this.scope = scope;
        this.javaReader = javaReader;
    }

    ExpressionNode readExpression(TypeName targetType, Expression expression) {
        ExpressionNode couscousExpression = readExpressionWithoutBoxing(expression);
        return coerceExpression(targetType, couscousExpression);
    }

    static ExpressionNode coerceExpression(TypeName targetType, ExpressionNode couscousExpression) {
        if (targetType.equals(couscousExpression.getType())) {
            return couscousExpression;
        } else {
            return typeCoercion(couscousExpression, targetType);
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

    ExpressionNode readExpressionWithoutBoxing(Expression expression) {
        switch (expression.getNodeType()) {
            case ASTNode.BOOLEAN_LITERAL:
                return readBooleanLiteral((BooleanLiteral)expression);

            case ASTNode.NUMBER_LITERAL:
                return readNumberLiteral((NumberLiteral)expression);

            case ASTNode.CHARACTER_LITERAL:
                return readCharacterLiteral((CharacterLiteral)expression);

            case ASTNode.STRING_LITERAL:
                return readStringLiteral((StringLiteral)expression);

            case ASTNode.TYPE_LITERAL:
                return readTypeLiteral((TypeLiteral)expression);

            case ASTNode.SIMPLE_NAME:
                return readSimpleName((SimpleName)expression);

            case ASTNode.THIS_EXPRESSION:
                return readThisExpression((ThisExpression)expression);

            case ASTNode.FIELD_ACCESS:
                return readFieldAccess((FieldAccess)expression);

            case ASTNode.METHOD_INVOCATION:
                return readMethodInvocation((MethodInvocation)expression);

            case ASTNode.CLASS_INSTANCE_CREATION:
                return readClassInstanceCreation((ClassInstanceCreation)expression);

            case ASTNode.LAMBDA_EXPRESSION:
                return readLambdaExpression((LambdaExpression)expression);

            case ASTNode.EXPRESSION_METHOD_REFERENCE:
                return readExpressionMethodReference((ExpressionMethodReference)expression);

            case ASTNode.INFIX_EXPRESSION:
                return readInfixExpression((InfixExpression)expression);

            case ASTNode.PREFIX_EXPRESSION:
                return readPrefixExpression((PrefixExpression)expression);

            case ASTNode.CONDITIONAL_EXPRESSION:
                return readConditionalExpression((ConditionalExpression)expression);

            case ASTNode.ASSIGNMENT:
                return readAssignment((Assignment)expression);

            case ASTNode.CAST_EXPRESSION:
                return readCastExpression((CastExpression)expression);

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

    private ExpressionNode readCharacterLiteral(CharacterLiteral expression) {
        // TODO: handle characters properly
        return literal(expression.charValue());
    }

    private static ExpressionNode readStringLiteral(StringLiteral expression) {
        return literal(expression.getLiteralValue());
    }

    private ExpressionNode readTypeLiteral(TypeLiteral expression) {
        return literal(typeOf(expression.getType()));
    }

    private ExpressionNode readSimpleName(SimpleName expression) {
        IBinding binding = expression.resolveBinding();
        if (binding.getKind() == IBinding.VARIABLE) {
            return readVariableBinding((IVariableBinding)binding);
        } else {
            throw new RuntimeException("Unsupported binding: " + binding.getClass());
        }
    }

    private ExpressionNode readVariableBinding(IVariableBinding binding) {
        TypeName type = typeOf(binding);
        if (binding.getDeclaringClass() == null) {
            return scope.reference(binding.getKey());
        } else {
            return FieldAccessNode.fieldAccess(ThisReferenceNode.thisReference(typeOf(binding.getDeclaringClass())), binding.getName(), type);
        }
    }

    private static ExpressionNode readThisExpression(ThisExpression expression) {
        return ThisReferenceNode.thisReference(typeOf(expression));
    }

    private ExpressionNode readFieldAccess(FieldAccess expression) {
        return FieldAccessNode.fieldAccess(readExpressionWithoutBoxing(expression.getExpression()), expression.getName().getIdentifier(), typeOf(expression));
    }

    private ExpressionNode readMethodInvocation(MethodInvocation expression) {
        String methodName = expression.getName().getIdentifier();
        @SuppressWarnings("unchecked")
        List<ExpressionNode> arguments = readArguments(
            expression.resolveMethodBinding(),
            expression.arguments());
        final TypeName type = typeOf(expression);

        IMethodBinding methodBinding = expression.resolveMethodBinding();
        TypeName receiverType = typeOf(methodBinding.getDeclaringClass());
        if ((Modifier.isStatic(methodBinding.getModifiers()))) {
            return staticMethodCall(
                receiverType,
                methodName,
                arguments,
                type);
        } else {
            ExpressionNode receiver = expression.getExpression() == null
                ? ThisReferenceNode.thisReference(receiverType)
                : readExpressionWithoutBoxing(expression.getExpression());
            return MethodCallNode.methodCall(
                receiver,
                methodName,
                arguments,
                type);
        }
    }

    private ExpressionNode readClassInstanceCreation(ClassInstanceCreation expression) {
        @SuppressWarnings("unchecked")
        List<Expression> javaArguments = expression.arguments();
        IMethodBinding constructor = expression.resolveConstructorBinding();
        List<ExpressionNode> arguments = readArguments(constructor, javaArguments);

        if (constructor.getDeclaringClass().isAnonymous()) {
            GeneratedClosure closure =
                javaReader.readAnonymousClass(scope, expression.getAnonymousClassDeclaration());
            return constructorCall(
                closure.getType(),
                concat(captureArguments(closure), arguments));
        } else {
            return constructorCall(typeOf(expression), arguments);
        }
    }

    private ExpressionNode readLambdaExpression(LambdaExpression expression) {
        GeneratedClosure closure = javaReader.readLambda(scope, expression);
        return constructorCall(
            closure.getType(),
            captureArguments(closure));
    }

    private ExpressionNode readExpressionMethodReference(ExpressionMethodReference expression) {
        GeneratedClosure closure = javaReader.readExpressionMethodReference(scope, expression);
        return constructorCall(
            closure.getType(),
            captureArguments(closure));
    }

    private List<? extends ExpressionNode> captureArguments(GeneratedClosure closure) {
        return closure.getCaptures();
    }

    private List<ExpressionNode> readArguments(IMethodBinding method, List<Expression> javaArguments) {
        return IntStream.range(0, javaArguments.size())
            .mapToObj(index -> readExpression(
                typeOf(method.getParameterTypes()[index]),
                javaArguments.get(index)))
            .collect(Collectors.toList());
    }

    private ExpressionNode readInfixExpression(InfixExpression expression) {
        if (isPrimitiveOperation(expression)) {
            Operator operator = readOperator(expression.getOperator());
            return readPrimitiveOperation(
                operator,
                expression.getLeftOperand(),
                expression.getRightOperand());
        } else {
            ExpressionNode left = readExpression(ObjectValues.OBJECT, expression.getLeftOperand());
            ExpressionNode right = readExpression(ObjectValues.OBJECT, expression.getRightOperand());
            if (expression.getOperator() == InfixExpression.Operator.EQUALS) {
                return Operations.same(left, right);
            } else if (expression.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
                return not(Operations.same(left, right));
            } else {
                throw new IllegalArgumentException("Unsupported operator: " + expression.getOperator());
            }
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

    private ExpressionNode readPrefixExpression(PrefixExpression expression) {
        if (expression.getOperator() == PrefixExpression.Operator.NOT) {
            return not(readExpression(BooleanValue.REF, expression.getOperand()));
        } else {
            Operator operator = readOperator(expression.getOperator());
            return AssignmentNode.assign(
                (AssignableExpressionNode) readExpressionWithoutBoxing(expression.getOperand()),
                MethodCallNode.methodCall(
                    readExpression(IntegerValue.REF, expression.getOperand()),
                    operator.getMethodName(),
                    list(literal(1)),
                    IntegerValue.REF));
        }
    }

    private ExpressionNode readConditionalExpression(ConditionalExpression expression) {
        TypeName type = typeOf(expression);
        return TernaryConditionalNode.ternaryConditional(
            readExpression(BooleanValue.REF, expression.getExpression()),
            readExpression(type, expression.getThenExpression()),
            readExpression(type, expression.getElseExpression()));
    }

    private ExpressionNode readAssignment(Assignment expression) {
        AssignableExpressionNode left = (AssignableExpressionNode)readExpressionWithoutBoxing(expression.getLeftHandSide());
        if (expression.getOperator() == Assignment.Operator.ASSIGN) {
            ExpressionNode right = readExpression(left.getType(), expression.getRightHandSide());
            return AssignmentNode.assign(left, right);
        } else {
            Operator operator = readOperator(expression.getOperator());
            return AssignmentNode.assign(
                left,
                coerceExpression(
                    left.getType(),
                    readPrimitiveOperation(operator, expression.getLeftHandSide(), expression.getRightHandSide())));
        }
    }

    private ExpressionNode readPrimitiveOperation(Operator operator, Expression leftJava, Expression rightJava) {
        ExpressionNode left = readUnboxedExpression(leftJava);
        ExpressionNode right = readUnboxedExpression(rightJava);
        return MethodCallNode.methodCall(
            left,
            operator.getMethodName(),
            list(right),
            operator.isBoolean() ? BooleanValue.REF : left.getType());
    }

    private ExpressionNode readCastExpression(CastExpression expression) {
        return cast(readExpressionWithoutBoxing(expression.getExpression()), typeOf(expression));
    }

    private ExpressionNode readUnboxedExpression(Expression expression) {
        return readExpression(
            unboxedType(typeOf(expression)),
            expression);
    }
}
