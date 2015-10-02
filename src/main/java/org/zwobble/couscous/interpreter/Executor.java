package org.zwobble.couscous.interpreter;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.zwobble.couscous.Project;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;
import org.zwobble.couscous.values.ConcreteType;
import org.zwobble.couscous.values.InterpreterValue;
import org.zwobble.couscous.values.StaticMethodValue;
import org.zwobble.couscous.values.UnitValue;

import static java.util.stream.Collectors.toMap;
import static org.zwobble.couscous.interpreter.Evaluator.eval;

import lombok.val;

public class Executor implements StatementNodeVisitor<Optional<InterpreterValue>> {
    public static StaticMethodValue callMethod(MethodNode method) {
        Supplier<List<ConcreteType<?>>> argumentTypes = () -> method.getArguments()
            .stream()
            .<ConcreteType<?>>map(arg -> arg.getType())
            .collect(Collectors.toList());
        return new StaticMethodValue(argumentTypes, arguments -> {
            val environment = buildEnvironment(method, arguments);
          
            for (val statement : method.getBody()) {
                val result = exec(environment, statement);
                if (result.isPresent()) {
                    return result.get();
                }
            }
            return UnitValue.UNIT;
        });
    }

    private static Environment buildEnvironment(
        final MethodNode method,
        Arguments arguments) {
        
        val stackFrame = IntStream.range(0, method.getArguments().size())
            .boxed()
            .collect(toMap(
                index -> method.getArguments().get(index).getId(),
                index -> arguments.get(index)));
        return new Environment(new Project() {
            @Override
            public ConcreteType<?> findClass(String name) {
                throw new UnsupportedOperationException();
            }
        }, stackFrame);
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
