package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.values.BooleanValue;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.frontends.java.JavaExpressionReader.readExpression;
import static org.zwobble.couscous.frontends.java.JavaExpressionReader.readExpressionWithoutBoxing;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class JavaStatementReader {
    static List<StatementNode> readStatement(Statement statement) {
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
}
