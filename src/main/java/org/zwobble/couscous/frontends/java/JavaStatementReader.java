package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.values.BooleanValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.frontends.java.JavaExpressionReader.readExpression;
import static org.zwobble.couscous.frontends.java.JavaExpressionReader.readExpressionWithoutBoxing;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.append;
import static org.zwobble.couscous.util.ExtraLists.concat;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

class JavaStatementReader {
    private final Optional<TypeName> returnType;

    JavaStatementReader(Optional<TypeName> returnType) {
        this.returnType = returnType;
    }

    List<StatementNode> readStatement(Statement statement) {
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

            case ASTNode.FOR_STATEMENT:
                return readForStatement((ForStatement)statement);

            case ASTNode.VARIABLE_DECLARATION_STATEMENT:
                return readVariableDeclarationStatement((VariableDeclarationStatement)statement);

            default:
                throw new RuntimeException("Unsupported statement: " + statement.getClass());
        }
    }

    private List<StatementNode> readBlock(Block block) {
        @SuppressWarnings("unchecked")
        List<Statement> statements = block.statements();
        return statements.stream()
            .flatMap(statement -> readStatement(statement).stream())
            .collect(Collectors.toList());
    }

    private StatementNode readReturnStatement(ReturnStatement statement) {
        return ReturnNode.returns(readExpression(returnType.get(), statement.getExpression()));
    }

    private static StatementNode readExpressionStatement(ExpressionStatement statement) {
        return expressionStatement(readExpressionWithoutBoxing(statement.getExpression()));
    }

    private StatementNode readIfStatement(IfStatement statement) {
        return IfStatementNode.ifStatement(
            readExpression(BooleanValue.REF, statement.getExpression()),
            readStatement(statement.getThenStatement()),
            readStatement(statement.getElseStatement()));
    }

    private WhileNode readWhileStatement(WhileStatement statement) {
        return whileLoop(
            readExpression(BooleanValue.REF, statement.getExpression()),
            readStatement(statement.getBody()));
    }

    private List<StatementNode> readForStatement(ForStatement statement) {
        List javaInitialisers = statement.initializers();
        if (javaInitialisers.size() != 1) {
            throw new UnsupportedOperationException();
        }
        VariableDeclarationExpression javaDeclaration = (VariableDeclarationExpression) javaInitialisers.get(0);
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = javaDeclaration.fragments();
        @SuppressWarnings("unchecked")
        List<Expression> updaters = statement.updaters();

        return append(
            readDeclarationFragments(fragments, typeOf(javaDeclaration.getType())),
            whileLoop(
                readExpression(BooleanValue.REF, statement.getExpression()),
                concat(
                    readStatement(statement.getBody()),
                    eagerMap(updaters, updater -> expressionStatement(readExpressionWithoutBoxing(updater))))));
    }

    private static List<StatementNode> readVariableDeclarationStatement(VariableDeclarationStatement statement) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = statement.fragments();
        TypeName type = typeOf(statement.getType());
        return readDeclarationFragments(fragments, type);
    }

    private static List<StatementNode> readDeclarationFragments(List<VariableDeclarationFragment> fragments, TypeName type) {
        return eagerMap(fragments, fragment ->
            localVariableDeclaration(
                fragment.resolveBinding().getKey(),
                fragment.getName().getIdentifier(),
                type,
                readExpression(type, fragment.getInitializer())));
    }
}
