package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.sugar.SwitchCaseNode;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.ObjectValues;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.tryFind;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.ast.sugar.SwitchCaseNode.switchCase;
import static org.zwobble.couscous.frontends.java.JavaExpressionReader.coerceExpression;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.*;
import static org.zwobble.couscous.util.Fold.foldRight;
import static org.zwobble.couscous.util.Tails.tail;
import static org.zwobble.couscous.util.Tails.tails;
import static org.zwobble.couscous.util.UpToAndIncludingIterable.upToAndIncluding;

class JavaStatementReader {
    private final Scope scope;
    private final JavaExpressionReader expressionReader;
    private final Optional<TypeName> returnType;

    JavaStatementReader(
        Scope scope,
        JavaExpressionReader expressionReader,
        Optional<TypeName> returnType
    ) {
        this.scope = scope;
        this.expressionReader = expressionReader;
        this.returnType = returnType;
    }

    List<StatementNode> readStatement(Statement statement) {
        switch (statement.getNodeType()) {
            case ASTNode.BLOCK:
                return readBlock((Block)statement);

            case ASTNode.RETURN_STATEMENT:
                return list(readReturnStatement((ReturnStatement)statement));

            case ASTNode.EXPRESSION_STATEMENT:
                return list(readExpressionStatement((ExpressionStatement)statement));

            case ASTNode.IF_STATEMENT:
                return list(readIfStatement((IfStatement)statement));

            case ASTNode.WHILE_STATEMENT:
                return list(readWhileStatement((WhileStatement)statement));

            case ASTNode.FOR_STATEMENT:
                return readForStatement((ForStatement)statement);

            case ASTNode.VARIABLE_DECLARATION_STATEMENT:
                return readVariableDeclarationStatement((VariableDeclarationStatement)statement);

            case ASTNode.SWITCH_STATEMENT:
                return readSwitchStatement((SwitchStatement)statement);

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

    private StatementNode readExpressionStatement(ExpressionStatement statement) {
        return expressionStatement(readExpressionWithoutBoxing(statement.getExpression()));
    }

    private StatementNode readIfStatement(IfStatement statement) {
        List<StatementNode> falseBranch = statement.getElseStatement() == null
            ? list()
            : readStatement(statement.getElseStatement());
        return ifStatement(
            readExpression(BooleanValue.REF, statement.getExpression()),
            readStatement(statement.getThenStatement()),
            falseBranch);
    }

    private List<StatementNode> readSwitchStatement(SwitchStatement switchStatement) {
        ExpressionNode switchValue = readExpressionWithoutBoxing(switchStatement.getExpression());
        LocalVariableDeclarationNode switchValueAssignment = scope.temporaryVariable(switchValue);

        @SuppressWarnings("unchecked")
        List<Statement> statements = switchStatement.statements();
        List<SwitchCaseNode> cases = eagerFlatMap(
            tails(statements),
            tail -> {
                Statement first = tail.get(0);
                if (first.getNodeType() == ASTNode.SWITCH_CASE) {
                    return list(readSwitchCase((SwitchCase) first, tail(tail)));
                } else {
                    return list();
                }
            });

        List<StatementNode> handleDefault = tryFind(cases, currentCase -> currentCase.isDefault())
            .transform(currentCase -> currentCase.getStatements())
            .or(list());

        return cons(
            switchValueAssignment,
            foldRight(cases, handleDefault, (handle, currentCase) ->
                currentCase.getValue()
                    .map(value -> list(ifStatement(
                        methodCall(
                            reference(switchValueAssignment),
                            "equals",
                            list(coerceExpression(ObjectValues.OBJECT, value)),
                            BooleanValue.REF),
                        currentCase.getStatements(), handle)))
                    .orElse(handle)));
    }

    private SwitchCaseNode readSwitchCase(SwitchCase caseStatement, List<Statement> statements)
    {
        Iterable<Statement> statementsForCase = upToAndIncluding(
            filter(
                statements,
                statement -> statement.getNodeType() != ASTNode.SWITCH_CASE),
            JavaStatementReader::isEndOfCase);
        return switchCase(
            Optional.ofNullable(caseStatement.getExpression()).map(this::readExpressionWithoutBoxing),
            readStatements(statementsForCase));
    }

    private static boolean isEndOfCase(Statement statement) {
        // TODO: do this on transformed nodes instead?
        return statement.getNodeType() == ASTNode.RETURN_STATEMENT;
    }

    private List<StatementNode> readStatements(Iterable<Statement> statements) {
        return eagerFlatMap(statements, this::readStatement);
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

    private List<StatementNode> readVariableDeclarationStatement(VariableDeclarationStatement statement) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = statement.fragments();
        TypeName type = typeOf(statement.getType());
        return readDeclarationFragments(fragments, type);
    }

    private List<StatementNode> readDeclarationFragments(
        List<VariableDeclarationFragment> fragments,
        TypeName type
    ) {
        return eagerMap(fragments, fragment -> scope.localVariable(
            fragment.resolveBinding().getKey(),
            fragment.getName().getIdentifier(),
            type,
            readExpression(type, fragment.getInitializer())));
    }

    private ExpressionNode readExpressionWithoutBoxing(Expression expression) {
        return expressionReader.readExpressionWithoutBoxing(expression);
    }

    private ExpressionNode readExpression(TypeName targetType, Expression expression) {
        return expressionReader.readExpression(targetType, expression);
    }
}
