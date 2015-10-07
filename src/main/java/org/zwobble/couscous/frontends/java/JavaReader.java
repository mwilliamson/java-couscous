package org.zwobble.couscous.frontends.java;

import java.nio.file.Path;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ClassNodeBuilder;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;

import lombok.SneakyThrows;
import lombok.val;

public class JavaReader {
    private final JavaParser parser = new JavaParser();
    
    @SneakyThrows
    public ClassNode readClassFromFile(Path path) {
        val ast = parser.parseCompilationUnit(path);
        return readCompilationUnit(ast);
    }
    
    public ClassNode readClassFromString(String source) {
        val ast = parser.parseCompilationUnit(source);
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

    private static String generateClassName(CompilationUnit ast) {
        val type = (TypeDeclaration)ast.types().get(0);
        return ast.getPackage().getName().getFullyQualifiedName() + "." + type.getName().getFullyQualifiedName();
    }
}
