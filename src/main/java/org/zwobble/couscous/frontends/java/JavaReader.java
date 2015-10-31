package org.zwobble.couscous.frontends.java;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.zwobble.couscous.ast.AssignableExpressionNode;
import org.zwobble.couscous.ast.AssignmentNode;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ClassNodeBuilder;
import org.zwobble.couscous.ast.ClassNodeBuilder.MethodBuilder;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.FieldAccessNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.ThisReferenceNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
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
        case ASTNode.RETURN_STATEMENT: 
            return asList(readReturnStatement((ReturnStatement)statement));
        
        case ASTNode.EXPRESSION_STATEMENT: 
            return asList(readExpressionStatement((ExpressionStatement)statement));
        
        case ASTNode.VARIABLE_DECLARATION_STATEMENT: 
            return readVariableDeclarationStatement((VariableDeclarationStatement)statement);
        
        default: 
            throw new RuntimeException("Unsupported statement: " + statement.getClass());
        
        }
    }
    
    private static StatementNode readReturnStatement(ReturnStatement statement) {
        return ReturnNode.returns(readExpression(statement.getExpression()));
    }
    
    private static StatementNode readExpressionStatement(ExpressionStatement statement) {
        return expressionStatement(readExpression(statement.getExpression()));
    }
    
    private static List<StatementNode> readVariableDeclarationStatement(VariableDeclarationStatement statement) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = (List<VariableDeclarationFragment>)statement.fragments();
        TypeName type = typeOf(statement.getType());
        return eagerMap(fragments, fragment ->
            localVariableDeclaration(
                fragment.resolveBinding().getKey(),
                fragment.getName().getIdentifier(),
                type,
                readExpression(fragment.getInitializer())));
    }
    
    private static ExpressionNode readExpression(Expression expression) {
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
        return FieldAccessNode.fieldAccess(readExpression(expression.getExpression()), expression.getName().getIdentifier(), typeOf(expression));
    }
    
    private static ExpressionNode readMethodInvocation(MethodInvocation expression) {
        String methodName = expression.getName().getIdentifier();
        List<ExpressionNode> arguments = readArguments(expression.arguments());
        if (expression.getExpression() == null) {
            IMethodBinding methodBinding = expression.resolveMethodBinding();
            TypeName receiverType = typeOf(methodBinding.getDeclaringClass());
            if ((methodBinding.getModifiers() & Modifier.STATIC) != 0) {
                return StaticMethodCallNode.staticMethodCall(receiverType, methodName, arguments);
            } else {
                return MethodCallNode.methodCall(ThisReferenceNode.thisReference(receiverType), methodName, arguments, typeOf(expression));
            }
        } else if (expression.getExpression().getNodeType() == ASTNode.SIMPLE_NAME) {
            Expression receiver = expression.getExpression();
            return StaticMethodCallNode.staticMethodCall(typeOf(receiver), methodName, arguments);
        } else {
            return MethodCallNode.methodCall(readExpression(expression.getExpression()), methodName, arguments, typeOf(expression));
        }
    }
    
    private static ExpressionNode readClassInstanceCreation(ClassInstanceCreation expression) {
        return ConstructorCallNode.constructorCall(typeOf(expression), readArguments(expression.arguments()));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<ExpressionNode> readArguments(final List javaArguments) {
        return eagerMap((List<Expression>)javaArguments, JavaReader::readExpression);
    }
    
    private static ExpressionNode readInfixExpression(InfixExpression expression) {
        final ExpressionNode left = readExpression(expression.getLeftOperand());
        final ExpressionNode right = readExpression(expression.getRightOperand());
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
        return TernaryConditionalNode.ternaryConditional(readExpression(expression.getExpression()), readExpression(expression.getThenExpression()), readExpression(expression.getElseExpression()));
    }
    
    private static ExpressionNode readAssignment(Assignment expression) {
        return AssignmentNode.assign((AssignableExpressionNode)readExpression(expression.getLeftHandSide()), readExpression(expression.getRightHandSide()));
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