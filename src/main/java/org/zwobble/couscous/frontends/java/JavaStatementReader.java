package org.zwobble.couscous.frontends.java;

import com.google.common.collect.Lists;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.sugar.SwitchCaseNode;
import org.zwobble.couscous.ast.sugar.SwitchNode;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

import java.util.List;
import java.util.Optional;

import static org.zwobble.couscous.ast.ExceptionHandlerNode.exceptionHandler;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.TryNode.tryStatement;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.ast.sugar.SwitchCaseNode.switchCase;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraIterables.only;
import static org.zwobble.couscous.util.ExtraIterables.takeUntil;
import static org.zwobble.couscous.util.ExtraLists.*;
import static org.zwobble.couscous.util.Tails.tail;
import static org.zwobble.couscous.util.Tails.tails;

class JavaStatementReader {
    private final Scope scope;
    private final JavaExpressionReader expressionReader;
    private final Optional<Type> returnType;

    JavaStatementReader(
        Scope scope,
        JavaExpressionReader expressionReader,
        Optional<Type> returnType
    ) {
        this.scope = scope;
        this.expressionReader = expressionReader;
        this.returnType = returnType;
    }

    List<StatementNode> readStatement(Statement statement) {
        try {
            switch (statement.getNodeType()) {
                case ASTNode.BLOCK:
                    return readBlock((Block)statement);

                case ASTNode.RETURN_STATEMENT:
                    return list(readReturnStatement((ReturnStatement)statement));

                case ASTNode.THROW_STATEMENT:
                    return list(readThrowStatement((ThrowStatement)statement));

                case ASTNode.EXPRESSION_STATEMENT:
                    return list(readExpressionStatement((ExpressionStatement)statement));

                case ASTNode.IF_STATEMENT:
                    return list(readIfStatement((IfStatement)statement));

                case ASTNode.WHILE_STATEMENT:
                    return list(readWhileStatement((WhileStatement)statement));

                case ASTNode.FOR_STATEMENT:
                    return readForStatement((ForStatement)statement);

                case ASTNode.ENHANCED_FOR_STATEMENT:
                    return readEnhancedForStatement((EnhancedForStatement)statement);

                case ASTNode.VARIABLE_DECLARATION_STATEMENT:
                    return readVariableDeclarationStatement((VariableDeclarationStatement)statement);

                case ASTNode.SWITCH_STATEMENT:
                    return readSwitchStatement((SwitchStatement)statement);

                case ASTNode.TRY_STATEMENT:
                    return readTryStatement((TryStatement)statement);

                default:
                    throw new RuntimeException("Unsupported statement: " + statement.getClass());
            }
        } catch (Exception exception) {
            if (statement == null || exception instanceof ReadError) {
                throw exception;
            } else {
                CompilationUnit root = (CompilationUnit) statement.getRoot();
                int lineNumber = root.getLineNumber(statement.getStartPosition());
                int columnNumber = root.getColumnNumber(statement.getStartPosition());
                throw new ReadError(
                    "Failed to read statement at " + lineNumber + ":" + columnNumber,
                    exception
                );
            }
        }
    }

    private List<StatementNode> readBlock(Block block) {
        @SuppressWarnings("unchecked")
        List<Statement> javaStatements = block.statements();
        List<StatementNode> statements = eagerFlatMap(javaStatements, this::readStatement);
        return list(new StatementBlockNode(statements));
    }

    private StatementNode readReturnStatement(ReturnStatement statement) {
        ExpressionNode value = statement.getExpression() == null
            ? LiteralNode.UNIT
            : readExpression(returnType.get(), statement.getExpression());
        return ReturnNode.returns(value);
    }

    private StatementNode readThrowStatement(ThrowStatement statement) {
        return ThrowNode.throwNode(readExpressionWithoutBoxing(statement.getExpression()));
    }

    private StatementNode readExpressionStatement(ExpressionStatement statement) {
        return expressionStatement(readExpressionWithoutBoxing(statement.getExpression()));
    }

    private StatementNode readIfStatement(IfStatement statement) {
        List<StatementNode> falseBranch = statement.getElseStatement() == null
            ? list()
            : readBody(statement.getElseStatement());
        return ifStatement(
            readExpression(Types.BOOLEAN, statement.getExpression()),
            readBody(statement.getThenStatement()),
            falseBranch);
    }

    private List<StatementNode> readSwitchStatement(SwitchStatement switchStatement) {
        ExpressionNode switchValue = readExpressionWithoutBoxing(switchStatement.getExpression());
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
        return list(new SwitchNode(switchValue, cases));
    }

    private SwitchCaseNode readSwitchCase(SwitchCase caseStatement, List<Statement> statements) {
        Iterable<Statement> statementsForCase = takeUntil(
            statements,
            statement -> statement.getNodeType() == ASTNode.SWITCH_CASE
        );
        return switchCase(
            Optional.ofNullable(caseStatement.getExpression()).map(this::readExpressionWithoutBoxing),
            readStatements(statementsForCase));
    }

    private List<StatementNode> readTryStatement(TryStatement statement) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationExpression> resources = statement.resources();
        List<LocalVariableDeclarationNode> resourceDeclarations = eagerFlatMap(resources, this::readVariableDeclarationExpression);
        List<StatementNode> body = readBody(statement.getBody());
        for (LocalVariableDeclarationNode resource : Lists.reverse(resourceDeclarations)) {
            body = list(
                resource,
                tryStatement(body, list(), list(expressionStatement(methodCall(reference(resource), "close", list(), Types.VOID))))
            );
        }
        @SuppressWarnings("unchecked")
        List<CatchClause> javaCatchClauses = statement.catchClauses();
        List<ExceptionHandlerNode> catchClauses = eagerMap(javaCatchClauses, this::readCatchClause);
        List<StatementNode> finallyBody = statement.getFinally() == null ? list() : readBody(statement.getFinally());
        if (catchClauses.isEmpty() && finallyBody.isEmpty() && !resourceDeclarations.isEmpty()) {
            return body;
        } else {
            return list(tryStatement(body, catchClauses, finallyBody));
        }
    }

    private ExceptionHandlerNode readCatchClause(CatchClause clause) {
        return exceptionHandler(
            JavaVariableDeclarationReader.read(scope, clause.getException()),
            readBody(clause.getBody()));
    }

    private List<StatementNode> readStatements(Iterable<Statement> statements) {
        return eagerFlatMap(statements, this::readStatement);
    }

    private WhileNode readWhileStatement(WhileStatement statement) {
        return whileLoop(
            readExpression(Types.BOOLEAN, statement.getExpression()),
            readBody(statement.getBody()));
    }

    private List<StatementNode> readForStatement(ForStatement statement) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationExpression> javaInitialisers = statement.initializers();
        @SuppressWarnings("unchecked")
        List<Expression> updaters = statement.updaters();

        return list(new ForNode(
            readVariableDeclarationExpression(only(javaInitialisers)),
            readExpression(Types.BOOLEAN, statement.getExpression()),
            eagerMap(updaters, this::readExpressionWithoutBoxing),
            readBody(statement.getBody())
        ));
    }

    private List<StatementNode> readEnhancedForStatement(EnhancedForStatement statement) {
        SingleVariableDeclaration parameter = statement.getParameter();
        Type elementType = typeOf(parameter.getType());

        ExpressionNode iterableValue = readExpression(JavaTypes.iterable(elementType), statement.getExpression());
        IVariableBinding parameterBinding = parameter.resolveBinding();

        ForEachNode node = new ForEachNode(
            scope.generateVariable(parameterBinding.getKey(), parameterBinding.getName(), elementType),
            iterableValue,
            readBody(statement.getBody())
        );
        return list(node);
    }

    private List<StatementNode> readVariableDeclarationStatement(VariableDeclarationStatement statement) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = statement.fragments();
        Type type = typeOf(statement.getType());
        return (List)readDeclarationFragments(fragments, type);
    }

    private List<LocalVariableDeclarationNode> readVariableDeclarationExpression(VariableDeclarationExpression expression) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = expression.fragments();
        return readDeclarationFragments(fragments, typeOf(expression.getType()));
    }

    private List<LocalVariableDeclarationNode> readDeclarationFragments(
        List<VariableDeclarationFragment> fragments,
        Type type
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

    private ExpressionNode readExpression(Type targetType, Expression expression) {
        return expressionReader.readExpression(targetType, expression);
    }

    List<StatementNode> readBody(Statement statement) {
        if (statement instanceof Block) {
            List<Statement> javaStatements = ((Block) statement).statements();
            return eagerFlatMap(javaStatements, this::readStatement);
        } else {
            return readStatement(statement);
        }
    }
}
