package org.zwobble.couscous.interpreter;

import java.util.Optional;

import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;
import org.zwobble.couscous.values.InterpreterValue;

import static org.zwobble.couscous.interpreter.Evaluator.eval;

public class Executor implements StatementNodeVisitor<Optional<InterpreterValue>> {
    public static Optional<InterpreterValue> exec(Environment environment, StatementNode statement) {
        return statement.accept(new Executor(environment));
    }
    
    private final Environment environment;

    private Executor(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public Optional<InterpreterValue> visit(ReturnNode returnNode) {
        return Optional.of(eval(environment, returnNode.getValue()));
    }

    @Override
    public Optional<InterpreterValue> visit(ExpressionStatementNode expressionStatement) {
        eval(environment, expressionStatement.getExpression());
        return Optional.empty();
    }
}
