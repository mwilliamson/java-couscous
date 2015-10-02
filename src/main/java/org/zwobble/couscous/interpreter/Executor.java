package org.zwobble.couscous.interpreter;

import java.util.Optional;
import java.util.stream.IntStream;

import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;
import org.zwobble.couscous.values.InterpreterValue;
import org.zwobble.couscous.values.UnitValue;

import static java.util.stream.Collectors.toMap;
import static org.zwobble.couscous.interpreter.Evaluator.eval;

import lombok.val;

public class Executor implements StatementNodeVisitor<Optional<InterpreterValue>> {
    public static InterpreterValue callMethod(Environment environment, MethodNode method, Arguments arguments) {
        val innerEnvironment = buildEnvironment(environment, method, arguments);
      
        for (val statement : method.getBody()) {
            val result = exec(innerEnvironment, statement);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return UnitValue.UNIT;
    }

    private static Environment buildEnvironment(
        final Environment environment,
        final MethodNode method,
        Arguments arguments) {
        
        val stackFrame = IntStream.range(0, method.getArguments().size())
            .boxed()
            .collect(toMap(
                index -> method.getArguments().get(index).getId(),
                index -> arguments.get(index)));
        return environment.withStackFrame(stackFrame);
    }
    
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
