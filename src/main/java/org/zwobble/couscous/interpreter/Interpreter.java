package org.zwobble.couscous.interpreter;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.zwobble.couscous.Project;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;
import org.zwobble.couscous.values.InterpreterValue;

import static com.google.common.collect.Iterables.find;
import static java.util.stream.Collectors.toMap;
import static org.zwobble.couscous.interpreter.Evaluator.eval;
import static org.zwobble.couscous.values.UnitValue.UNIT;

import lombok.val;

public class Interpreter {
    private Project project;

    public Interpreter(Project project) {
        this.project = project;
    }
    
    public InterpreterValue run(String className, String methodName, List<InterpreterValue> arguments) {
        val clazz = project.findClass(className);
        val method = find(clazz.getMethods(),
            method -> method.getName().equals(methodName));
        val environment = buildEnvironment(method, arguments);
        
        for (val statement : method.getBody()) {
            val result = statement.accept(new Executor(environment));
            if (result.isPresent()) {
                return result.get();
            }
        }
        return UNIT;
    }

    private Environment buildEnvironment(
        final MethodNode method,
        List<InterpreterValue> arguments) {
        
        if (method.getArguments().size() != arguments.size()) {
            throw new WrongNumberOfArguments(method.getArguments().size(), arguments.size());
        }
        
        val stackFrame = IntStream.range(0, method.getArguments().size())
            .boxed()
            .collect(toMap(
                index -> method.getArguments().get(index).getId(),
                index -> arguments.get(index)));
        return new Environment(stackFrame);
    }
    
    private class Executor implements StatementNodeVisitor<Optional<InterpreterValue>> {
        private Environment environment;

        public Executor(Environment environment) {
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
}
