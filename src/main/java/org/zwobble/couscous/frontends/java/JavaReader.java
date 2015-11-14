package org.zwobble.couscous.frontends.java;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.ClassNodeBuilder.MethodBuilder;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class JavaReader {
    
    private final JavaParser parser = new JavaParser();
    
    public ClassNode readClassFromFile(Path root, Path sourcePath) throws IOException {
        CompilationUnit ast = parser.parseCompilationUnit(root, sourcePath);
        return readCompilationUnit(ast);
    }
    
    private static ClassNode readCompilationUnit(CompilationUnit ast) {
        String name = generateClassName(ast);
        ClassNodeBuilder classBuilder = new ClassNodeBuilder(name);
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        readFields(type, classBuilder);
        readMethods(type, classBuilder);
        return classBuilder.build();
    }
    
    private static void readFields(TypeDeclaration type, ClassNodeBuilder classBuilder) {
        for (FieldDeclaration field : type.getFields()) {
            readField(field, classBuilder);
        }
    }
    
    private static void readField(FieldDeclaration field, ClassNodeBuilder classBuilder) {
        for (Object fragment : field.fragments()) {
            String name = ((VariableDeclarationFragment)fragment).getName().getIdentifier();
            classBuilder.field(name, typeOf(field.getType()));
        }
    }
    
    private static void readMethods(TypeDeclaration type, ClassNodeBuilder classBuilder) {
        for (MethodDeclaration method : type.getMethods()) {
            readMethod(classBuilder, method);
        }
    }
    
    private static void readMethod(ClassNodeBuilder classBuilder, MethodDeclaration method) {
        if (method.isConstructor()) {
            classBuilder.constructor(builder -> buildMethod(method, builder));
        } else {
            classBuilder.method(method.getName().getIdentifier(), true, builder -> buildMethod(method, builder));
        }
    }
    
    private static <T> MethodBuilder<T> buildMethod(MethodDeclaration method, MethodBuilder<T> builder) {
        for (IAnnotationBinding annotation : method.resolveBinding().getAnnotations()) {
            builder.annotation(typeOf(annotation.getAnnotationType()));
        }
        for (Object parameterObject : method.parameters()) {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration)parameterObject;
            builder.argument(parameter.resolveBinding().getKey(), parameter.getName().getIdentifier(), typeOf(parameter.resolveBinding()));
        }
        for (Object statement : method.getBody().statements()) {
            for (StatementNode intermediateStatement : readStatement((Statement)statement)) {
                builder.statement(intermediateStatement);
            }
        }
        return builder;
    }
    
    private static List<StatementNode> readStatement(Statement statement) {
        switch (statement.getNodeType()) {
            case ASTNode.BLOCK:
                return readBlock((Block)statement);

            case ASTNode.RETURN_STATEMENT:
                return asList(readReturnStatement((ReturnStatement)statement));

            case ASTNode.EXPRESSION_STATEMENT:
                return asList(readExpressionStatement((ExpressionStatement)statement));

            case ASTNode.IF_STATEMENT:
                return asList(readIfStatement((IfStatement)statement));

            case ASTNode.WHILE_STATEMENT:
                return asList(readWhileStatement((WhileStatement)statement));

            case ASTNode.VARIABLE_DECLARATION_STATEMENT:
                return readVariableDeclarationStatement((VariableDeclarationStatement)statement);

            default:
                throw new RuntimeException("Unsupported statement: " + statement.getClass());
        }
    }

    private static List<StatementNode> readBlock(Block block) {
        @SuppressWarnings("unchecked")
        List<Statement> statements = block.statements();
        return statements.stream()
            .flatMap(statement -> readStatement(statement).stream())
            .collect(Collectors.toList());
    }

    private static StatementNode readReturnStatement(ReturnStatement statement) {
        // TODO: set target type
        return ReturnNode.returns(readExpressionWithoutBoxing(statement.getExpression()));
    }
    
    private static StatementNode readExpressionStatement(ExpressionStatement statement) {
        return expressionStatement(readExpressionWithoutBoxing(statement.getExpression()));
    }
    
    private static StatementNode readIfStatement(IfStatement statement) {
        return IfStatementNode.ifStatement(
            readExpression(BooleanValue.REF, statement.getExpression()),
            readStatement(statement.getThenStatement()),
            readStatement(statement.getElseStatement()));
    }

    private static WhileNode readWhileStatement(WhileStatement statement) {
        return whileLoop(
            readExpression(BooleanValue.REF, statement.getExpression()),
            readStatement(statement.getBody()));
    }

    private static List<StatementNode> readVariableDeclarationStatement(VariableDeclarationStatement statement) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = statement.fragments();
        TypeName type = typeOf(statement.getType());
        return eagerMap(fragments, fragment ->
            localVariableDeclaration(
                fragment.resolveBinding().getKey(),
                fragment.getName().getIdentifier(),
                type,
                readExpression(type, fragment.getInitializer())));
    }
    
    private static ExpressionNode readExpression(TypeName targetType, Expression expression) {
        ExpressionNode couscousExpression = readExpressionWithoutBoxing(expression);
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

    private static ExpressionNode readExpressionWithoutBoxing(Expression expression) {
        switch (expression.getNodeType()) {
        case ASTNode.BOOLEAN_LITERAL: 
            return readBooleanLiteral((BooleanLiteral)expression);
        
        case ASTNode.NUMBER_LITERAL: 
            return readNumberLiteral((NumberLiteral)expression);
        
        case ASTNode.STRING_LITERAL: 
            return readStringLiteral((StringLiteral)expression);
        
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
        
        case ASTNode.INFIX_EXPRESSION:
            return readInfixExpression((InfixExpression)expression);
            
        case ASTNode.CONDITIONAL_EXPRESSION: 
            return readConditionalExpression((ConditionalExpression)expression);
        
        case ASTNode.ASSIGNMENT: 
            return readAssignment((Assignment)expression);
        
        default: 
            throw new RuntimeException("Unsupported expression: " + expression.getClass());
        
        }
    }

    private static ExpressionNode readBooleanLiteral(BooleanLiteral expression) {
        return LiteralNode.literal(expression.booleanValue());
    }
    
    private static LiteralNode readNumberLiteral(NumberLiteral expression) {
        return LiteralNode.literal(Integer.parseInt(expression.getToken()));
    }
    
    private static ExpressionNode readStringLiteral(StringLiteral expression) {
        return LiteralNode.literal(expression.getLiteralValue());
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
    
    private static ExpressionNode readFieldAccess(FieldAccess expression) {
        return FieldAccessNode.fieldAccess(readExpressionWithoutBoxing(expression.getExpression()), expression.getName().getIdentifier(), typeOf(expression));
    }
    
    private static ExpressionNode readMethodInvocation(MethodInvocation expression) {
        String methodName = expression.getName().getIdentifier();
        @SuppressWarnings("unchecked")
        List<ExpressionNode> arguments = readArguments(
            expression.resolveMethodBinding(),
            expression.arguments());
        final TypeName type = typeOf(expression);
        
        IMethodBinding methodBinding = expression.resolveMethodBinding();
        TypeName receiverType = typeOf(methodBinding.getDeclaringClass());
        if ((methodBinding.getModifiers() & Modifier.STATIC) != 0) {
            return StaticMethodCallNode.staticMethodCall(
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
    
    private static ExpressionNode readClassInstanceCreation(ClassInstanceCreation expression) {
        @SuppressWarnings("unchecked")
        List<Expression> arguments = expression.arguments();
        return ConstructorCallNode.constructorCall(
            typeOf(expression),
            readArguments(expression.resolveConstructorBinding(), arguments));
    }
    
    private static List<ExpressionNode> readArguments(IMethodBinding method, List<Expression> javaArguments) {
        return IntStream.range(0, javaArguments.size())
            .mapToObj(index -> readExpression(
                typeOf(method.getParameterTypes()[index]),
                javaArguments.get(index)))
            .collect(Collectors.toList());
    }
    
    private static ExpressionNode readInfixExpression(InfixExpression expression) {
        if (typeOf(expression.getLeftOperand()).equals(IntegerValue.REF)) {
            ExpressionNode left = readExpression(IntegerValue.REF, expression.getLeftOperand());
            ExpressionNode right = readExpression(IntegerValue.REF, expression.getRightOperand());
            
            if (expression.getOperator() == Operator.NOT_EQUALS) {
                return MethodCallNode.not(methodCall(left, "equals", asList(right), BooleanValue.REF));
            } else {
                JavaOperator operator = readOperator(expression.getOperator());
                return MethodCallNode.methodCall(
                    left,
                    operator.methodName,
                    asList(right),
                    operator.returnValue);
            }
        } else {
            ExpressionNode left = readExpression(ObjectValues.OBJECT, expression.getLeftOperand());
            ExpressionNode right = readExpression(ObjectValues.OBJECT, expression.getRightOperand());
            if (expression.getOperator() == Operator.EQUALS) {
                return StaticMethodCallNode.same(left, right);
            } else if (expression.getOperator() == Operator.NOT_EQUALS) {
                return MethodCallNode.not(StaticMethodCallNode.same(left, right));
            } else {
                throw new IllegalArgumentException("Unsupported operator: " + expression.getOperator());
            }
        }
    }
    
    private static JavaOperator readOperator(Operator operator) {
        if (operator == Operator.PLUS) {
            return JavaOperator.of("add", IntegerValue.REF);
        } else if (operator == Operator.MINUS) {
            return JavaOperator.of("subtract", IntegerValue.REF);
        } else if (operator == Operator.TIMES) {
            return JavaOperator.of("multiply", IntegerValue.REF);
        } else if (operator == Operator.DIVIDE) {
            return JavaOperator.of("divide", IntegerValue.REF);
        } else if (operator == Operator.REMAINDER) {
            return JavaOperator.of("mod", IntegerValue.REF);
        } else if (operator == Operator.EQUALS) {
            return JavaOperator.of("equals", BooleanValue.REF);
        } else if (operator == Operator.GREATER) {
            return JavaOperator.of("greaterThan", BooleanValue.REF);
        } else if (operator == Operator.GREATER_EQUALS) {
            return JavaOperator.of("greaterThanOrEqual", BooleanValue.REF);
        } else if (operator == Operator.LESS) {
            return JavaOperator.of("lessThan", BooleanValue.REF);
        } else if (operator == Operator.LESS_EQUALS) {
            return JavaOperator.of("lessThanOrEqual", BooleanValue.REF);
        } else {
            throw new RuntimeException("Unrecognised operator: " + operator);
        }
    }
    
    private static class JavaOperator {
        private static JavaOperator of(String methodName, TypeName returnValue) {
            return new JavaOperator(methodName, returnValue);
        }
        
        private final String methodName;
        private final TypeName returnValue;

        private JavaOperator(String methodName, TypeName returnValue) {
            this.methodName = methodName;
            this.returnValue = returnValue;
        }
    }

    private static ExpressionNode readConditionalExpression(ConditionalExpression expression) {
        TypeName type = typeOf(expression);
        return TernaryConditionalNode.ternaryConditional(
            readExpression(BooleanValue.REF, expression.getExpression()),
            readExpression(type, expression.getThenExpression()),
            readExpression(type, expression.getElseExpression()));
    }
    
    private static ExpressionNode readAssignment(Assignment expression) {
        ExpressionNode left = readExpressionWithoutBoxing(expression.getLeftHandSide());
        return AssignmentNode.assign(
            (AssignableExpressionNode)left,
            readExpression(left.getType(), expression.getRightHandSide()));
    }
    
    private static String generateClassName(CompilationUnit ast) {
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        return ast.getPackage().getName().getFullyQualifiedName() + "." + type.getName().getFullyQualifiedName();
    }
    
    private static TypeName typeOf(Expression expression) {
        return typeOf(expression.resolveTypeBinding());
    }
    
    private static TypeName typeOf(IVariableBinding variableBinding) {
        return typeOf(variableBinding.getType());
    }
    
    private static TypeName typeOf(Type type) {
        return typeOf(type.resolveBinding());
    }
    
    private static TypeName typeOf(ITypeBinding typeBinding) {
        return TypeName.of(typeBinding.getQualifiedName());
    }
}