package org.zwobble.couscous.frontends.java;

import com.google.common.collect.Iterables;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.ArrayNode.array;
import static org.zwobble.couscous.ast.CastNode.cast;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.InstanceReceiver.instanceReceiver;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.OperationNode.operation;
import static org.zwobble.couscous.ast.Operations.instanceOf;
import static org.zwobble.couscous.ast.Operations.not;
import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.TypeCoercionNode.coerce;
import static org.zwobble.couscous.frontends.java.JavaOperators.readOperator;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.Fold.foldLeft;

public class JavaExpressionReader {
    private final Scope scope;
    private final JavaReader javaReader;

    JavaExpressionReader(Scope scope, JavaReader javaReader) {
        this.scope = scope;
        this.javaReader = javaReader;
    }

    ExpressionNode readExpression(Type targetType, Expression expression) {
        ExpressionNode couscousExpression = readExpressionWithoutBoxing(expression);
        return coerce(couscousExpression, targetType);
    }

    private static Type unboxedType(Type type) {
        if (type.equals(Types.BOXED_INT)) {
            return Types.INT;
        } else if (type.equals(Types.BOXED_BOOLEAN)) {
            return Types.BOOLEAN;
        } else {
            return type;
        }
    }

    ExpressionNode readExpressionWithoutBoxing(Expression expression) {
        try {

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

                case ASTNode.ARRAY_CREATION:
                    return readArrayCreation((ArrayCreation)expression);

                case ASTNode.PARENTHESIZED_EXPRESSION:
                    return readParenthesizedExpression((ParenthesizedExpression)expression);

                case ASTNode.SIMPLE_NAME:
                    return readSimpleName((SimpleName)expression);

                case ASTNode.THIS_EXPRESSION:
                    return readThisExpression((ThisExpression)expression);

                case ASTNode.FIELD_ACCESS:
                    return readFieldAccess((FieldAccess)expression);

                case ASTNode.QUALIFIED_NAME:
                    return readQualifiedName((QualifiedName)expression);

                case ASTNode.METHOD_INVOCATION:
                    return readMethodInvocation((MethodInvocation)expression);

                case ASTNode.CLASS_INSTANCE_CREATION:
                    return readClassInstanceCreation((ClassInstanceCreation)expression);

                case ASTNode.LAMBDA_EXPRESSION:
                    return readLambdaExpression((LambdaExpression)expression);

                case ASTNode.EXPRESSION_METHOD_REFERENCE:
                    return readExpressionMethodReference((ExpressionMethodReference)expression);

                case ASTNode.CREATION_REFERENCE:
                    return readCreationReference((CreationReference)expression);

                case ASTNode.INFIX_EXPRESSION:
                    return readInfixExpression((InfixExpression)expression);

                case ASTNode.INSTANCEOF_EXPRESSION:
                    return readInstanceofExpression((InstanceofExpression)expression);

                case ASTNode.PREFIX_EXPRESSION:
                    return readPrefixExpression((PrefixExpression)expression);

                case ASTNode.CONDITIONAL_EXPRESSION:
                    return readConditionalExpression((ConditionalExpression)expression);

                case ASTNode.ASSIGNMENT:
                    return readAssignment((Assignment)expression);

                case ASTNode.CAST_EXPRESSION:
                    return readCastExpression((CastExpression)expression);

                default:
                    throw new RuntimeException(
                        "Unsupported expression: " + expression.getClass());

            }
        } catch (Exception exception) {
            if (exception instanceof ExpressionReadError) {
                throw exception;
            } else {
                CompilationUnit root = (CompilationUnit) expression.getRoot();
                int lineNumber = root.getLineNumber(expression.getStartPosition());
                int columnNumber = root.getColumnNumber(expression.getStartPosition());
                throw new ExpressionReadError(
                    "Failed to read expression at " + lineNumber + ":" + columnNumber,
                    exception);
            }
        }
    }

    private static ExpressionNode readBooleanLiteral(BooleanLiteral expression) {
        return literal(expression.booleanValue());
    }

    private static LiteralNode readNumberLiteral(NumberLiteral expression) {
        return literal(Integer.parseInt(expression.getToken()));
    }

    private ExpressionNode readCharacterLiteral(CharacterLiteral expression) {
        return literal(expression.charValue());
    }

    private static ExpressionNode readStringLiteral(StringLiteral expression) {
        return literal(expression.getLiteralValue());
    }

    private ExpressionNode readTypeLiteral(TypeLiteral expression) {
        return literal(erasure(typeOf(expression.getType())));
    }

    private ExpressionNode readArrayCreation(ArrayCreation expression) {
        if (expression.getType().getDimensions() != 1) {
            throw new UnsupportedOperationException();
        }
        Type elementType = typeOf(expression.getType().getElementType());
        @SuppressWarnings("unchecked")
        List<Expression> expressions = expression.getInitializer().expressions();
        return array(elementType, eagerMap(expressions, element -> readExpression(elementType, element)));
    }

    private ExpressionNode readParenthesizedExpression(ParenthesizedExpression expression) {
        return readExpressionWithoutBoxing(expression.getExpression());
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
        if (binding.getDeclaringClass() == null) {
            return scope.reference(binding.getKey());
        } else {
            boolean isStatic = Modifier.isStatic(binding.getModifiers());
            Type classType = typeOf(binding.getDeclaringClass());
            Receiver receiver = isStatic
                ? new StaticReceiver(erasure(classType))
                : new InstanceReceiver(thisReference(classType));
            return FieldAccessNode.fieldAccess(
                receiver,
                binding.getName(),
                typeOf(binding));
        }
    }

    private static ExpressionNode readThisExpression(ThisExpression expression) {
        return thisReference(typeOf(expression));
    }

    private ExpressionNode readFieldAccess(FieldAccess expression) {
        IVariableBinding fieldBinding = expression.resolveFieldBinding();
        boolean isStatic = Modifier.isStatic(fieldBinding.getModifiers());
        Receiver receiver = isStatic
            ? new StaticReceiver(erasure(typeOf(fieldBinding.getDeclaringClass())))
            : new InstanceReceiver(readExpressionWithoutBoxing(expression.getExpression()));

        return FieldAccessNode.fieldAccess(
            receiver,
            expression.getName().getIdentifier(),
            typeOf(expression));
    }

    private ExpressionNode readQualifiedName(QualifiedName expression) {
        Receiver receiver = toReceiver(expression.getQualifier().resolveBinding());
        return FieldAccessNode.fieldAccess(
            receiver,
            expression.getName().getIdentifier(),
            typeOf(expression));
    }

    private Receiver toReceiver(IBinding binding) {
        if (binding instanceof ITypeBinding) {
            ScalarType receiver = ScalarType.topLevel(((ITypeBinding) binding).getQualifiedName());
            return staticReceiver(receiver);
        } else if (binding instanceof IVariableBinding) {
            return instanceReceiver(readVariableBinding((IVariableBinding) binding));
        } else {
            throw new UnsupportedOperationException("Unsupported binding: " + binding);
        }
    }

    private ExpressionNode readMethodInvocation(MethodInvocation expression) {
        String methodName = expression.getName().getIdentifier();
        @SuppressWarnings("unchecked")
        List<ITypeBinding> typeArguments = asList(expression.resolveMethodBinding().getTypeArguments());
        List<Type> typeParameters = eagerMap(typeArguments, JavaTypes::typeOf);
        @SuppressWarnings("unchecked")
        List<ExpressionNode> arguments = readArguments(
            expression.resolveMethodBinding(),
            expression.arguments());
        Type returnType = typeOf(expression);
        MethodSignature signature = JavaMethods.signature(expression.resolveMethodBinding().getMethodDeclaration());

        return methodCall(
            getReceiver(expression),
            methodName,
            typeParameters,
            arguments,
            returnType,
            signature);
    }

    private Receiver getReceiver(MethodInvocation expression) {
        IMethodBinding methodBinding = expression.resolveMethodBinding();
        Type receiverType = typeOf(methodBinding.getDeclaringClass());
        if ((Modifier.isStatic(methodBinding.getModifiers()))) {
            return staticReceiver(erasure(receiverType));
        } else {
            ExpressionNode receiver = expression.getExpression() == null
                ? thisReference(receiverType)
                : readExpressionWithoutBoxing(expression.getExpression());
            return instanceReceiver(receiver);
        }
    }

    private ExpressionNode readClassInstanceCreation(ClassInstanceCreation expression) {
        @SuppressWarnings("unchecked")
        List<Expression> javaArguments = expression.arguments();
        IMethodBinding constructor = expression.resolveConstructorBinding();
        List<ExpressionNode> arguments = readArguments(constructor, javaArguments);

        ITypeBinding declaringClass = constructor.getDeclaringClass();
        if (declaringClass.isAnonymous()) {
            return javaReader.readAnonymousClass(scope, expression.getAnonymousClassDeclaration());
        } else {
            if (expression.getExpression() != null) {
                throw new UnsupportedOperationException();
            }
            return constructorCall(typeOf(expression), arguments);
        }
    }

    private ExpressionNode readLambdaExpression(LambdaExpression expression) {
        return javaReader.readLambda(scope, expression);
    }

    private ExpressionNode readExpressionMethodReference(ExpressionMethodReference expression) {
        return javaReader.readExpressionMethodReference(scope, expression);
    }

    private ExpressionNode readCreationReference(CreationReference expression) {
        return javaReader.readCreationReference(scope, expression);
    }

    private List<ExpressionNode> readArguments(IMethodBinding method, List<Expression> javaArguments) {
        ITypeBinding[] parameterTypes = method.getParameterTypes();
        return IntStream.range(0, parameterTypes.length)
            .mapToObj(index -> {
                boolean isVarargs =
                    method.isVarargs() &&
                    index == parameterTypes.length - 1 &&
                    (javaArguments.size() != parameterTypes.length || !javaArguments.get(index).resolveTypeBinding().isEqualTo(parameterTypes[index]));
                if (isVarargs) {
                    Type elementType = typeOf(parameterTypes[index].getElementType());
                    List<ExpressionNode> elements = IntStream.range(index, javaArguments.size())
                        .mapToObj(argumentIndex -> readExpression(elementType, javaArguments.get(argumentIndex)))
                        .collect(Collectors.toList());
                    return array(elementType, elements);
                } else {
                    return readExpression(
                        typeOf(parameterTypes[index]),
                        javaArguments.get(index));
                }
            })
            .collect(Collectors.toList());
    }

    private ExpressionNode readInfixExpression(InfixExpression expression) {
        if (isPrimitiveOperation(expression)) {
            Operator operator = readOperator(expression.getOperator());
            return readPrimitiveOperation(operator, operands(expression));
        } else {
            ExpressionNode left = readExpression(Types.OBJECT, expression.getLeftOperand());
            ExpressionNode right = readExpression(Types.OBJECT, expression.getRightOperand());
            if (expression.getOperator() == InfixExpression.Operator.EQUALS) {
                return Operations.same(left, right);
            } else if (expression.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
                return not(Operations.same(left, right));
            } else {
                throw new IllegalArgumentException("Unsupported operator: " + expression.getOperator());
            }
        }
    }

    private Iterable<Expression> operands(InfixExpression expression) {
        @SuppressWarnings("unchecked")
        List<Expression> extendedOperands = expression.extendedOperands();
        return Iterables.concat(
            list(expression.getLeftOperand()),
            list(expression.getRightOperand()),
            extendedOperands);
    }

    private static boolean isPrimitiveOperation(InfixExpression expression) {
        // TODO: consider expression.extendedOperands
        Type leftOperandType = typeOf(expression.getLeftOperand());
        Type rightOperandType = typeOf(expression.getRightOperand());
        return isPrimitive(leftOperandType)
            || isPrimitive(rightOperandType)
            || (expression.getOperator() != InfixExpression.Operator.NOT_EQUALS
            && expression.getOperator() != InfixExpression.Operator.EQUALS);
    }

    private static boolean isPrimitive(Type type) {
        return type.equals(Types.BOOLEAN) || type.equals(Types.INT);
    }

    private ExpressionNode readInstanceofExpression(InstanceofExpression expression) {
        return instanceOf(
            readExpressionWithoutBoxing(expression.getLeftOperand()),
            (ScalarType)typeOf(expression.getRightOperand()));
    }

    private ExpressionNode readPrefixExpression(PrefixExpression expression) {
        if (expression.getOperator() == PrefixExpression.Operator.NOT) {
            return not(readExpression(Types.BOOLEAN, expression.getOperand()));
        } else {
            Operator operator = readOperator(expression.getOperator());
            return AssignmentNode.assign(
                (AssignableExpressionNode) readExpressionWithoutBoxing(expression.getOperand()),
                operation(
                    operator,
                    list(
                        readExpression(Types.INT, expression.getOperand()),
                        literal(1)),
                    Types.INT));
        }
    }

    private ExpressionNode readConditionalExpression(ConditionalExpression expression) {
        Type type = typeOf(expression);
        return TernaryConditionalNode.ternaryConditional(
            readExpression(Types.BOOLEAN, expression.getExpression()),
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
                coerce(readPrimitiveOperation(operator, list(expression.getLeftHandSide(), expression.getRightHandSide())), left.getType()));
        }
    }

    private ExpressionNode readPrimitiveOperation(Operator operator, Iterable<Expression> javaOperands) {
        List<ExpressionNode> operands = eagerMap(javaOperands, operand -> readUnboxedExpression(operand));
        return foldLeft(operands, (left, right) -> operation(
            operator,
            list(left, right),
            operator.isBoolean() ? Types.BOOLEAN : left.getType())
        );
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
