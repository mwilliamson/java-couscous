package org.zwobble.couscous.frontends.java;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ClassNodeBuilder;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.ThisReferenceNode;
import org.zwobble.couscous.ast.TypeName;

import com.google.common.collect.Lists;

import lombok.SneakyThrows;
import lombok.val;

public class JavaReader {
    private final JavaParser parser = new JavaParser();
    
    @SneakyThrows
    public ClassNode readClassFromFile(Path root, Path sourcePath) {
        val ast = parser.parseCompilationUnit(root, sourcePath);
        return readCompilationUnit(ast);
    }
        
    private static ClassNode readCompilationUnit(CompilationUnit ast) {
        val name = generateClassName(ast);

        val classBuilder = new ClassNodeBuilder(name);

        val type = (TypeDeclaration)ast.types().get(0);
        
        for (val method : type.getMethods()) {
            classBuilder.method(
                method.getName().getIdentifier(),
                true,
                builder -> {
                    for (Object statement : method.getBody().statements()) {
                        builder.statement(readStatement((Statement)statement));
                    }
                    return builder;
                });
        }
        
        return classBuilder
            .build();
    }

    private static StatementNode readStatement(Statement statement) {
        switch (statement.getNodeType()) {
            case ASTNode.RETURN_STATEMENT:
                return readReturnStatement((ReturnStatement)statement);
            default:
                throw new RuntimeException("Unsupported statement: " + statement.getClass());
        }
    }

    private static StatementNode readReturnStatement(ReturnStatement statement) {
        return new ReturnNode(readExpression(statement.getExpression()));
    }

    private static ExpressionNode readExpression(Expression expression) {
        switch (expression.getNodeType()) {
            case ASTNode.BOOLEAN_LITERAL:
                return readBooleanLiteral((BooleanLiteral)expression);
            case ASTNode.NUMBER_LITERAL:
                return readNumberLiteral((NumberLiteral)expression);
            case ASTNode.STRING_LITERAL:
                return readStringLiteral((StringLiteral)expression);
            case ASTNode.THIS_EXPRESSION:
                return readThisExpression((ThisExpression)expression);
            case ASTNode.METHOD_INVOCATION:
                return readMethodInvocation((MethodInvocation)expression);
            case ASTNode.CLASS_INSTANCE_CREATION:
                return readClassInstanceCreation((ClassInstanceCreation)expression);
            case ASTNode.CONDITIONAL_EXPRESSION:
                return readConditionalExpression((ConditionalExpression)expression);
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

    private static ExpressionNode readThisExpression(ThisExpression expression) {
        return ThisReferenceNode.thisReference(
            TypeName.of(expression.resolveTypeBinding().getQualifiedName()));
    }

    private static ExpressionNode readMethodInvocation(MethodInvocation expression) {
        val methodName = expression.getName().getIdentifier();
        val arguments = readArguments(expression.arguments());
        if (expression.getExpression().getNodeType() == ASTNode.SIMPLE_NAME) {
            val receiver = expression.getExpression().resolveTypeBinding();
            return StaticMethodCallNode.staticMethodCall(receiver.getQualifiedName(), methodName, arguments);
        } else {
            return MethodCallNode.methodCall(
                readExpression(expression.getExpression()),
                methodName,
                arguments,
                TypeName.of(expression.resolveTypeBinding().getQualifiedName()));
        }
    }

    private static ExpressionNode readClassInstanceCreation(ClassInstanceCreation expression) {
        return ConstructorCallNode.constructorCall(
            TypeName.of(expression.resolveTypeBinding().getQualifiedName()),
            readArguments(expression.arguments()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<ExpressionNode> readArguments(final List javaArguments) {
        return Lists.transform(
            (List<Expression>)javaArguments,
            JavaReader::readExpression);
    }

    private static ExpressionNode readConditionalExpression(ConditionalExpression expression) {
        return new TernaryConditionalNode(
            readExpression(expression.getExpression()),
            readExpression(expression.getThenExpression()),
            readExpression(expression.getElseExpression()));
    }

    private static String generateClassName(CompilationUnit ast) {
        val type = (TypeDeclaration)ast.types().get(0);
        return ast.getPackage().getName().getFullyQualifiedName() + "." + type.getName().getFullyQualifiedName();
    }
}
