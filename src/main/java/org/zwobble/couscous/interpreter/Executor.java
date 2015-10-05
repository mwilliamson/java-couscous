package org.zwobble.couscous.interpreter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.UnitInterpreterValue;

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
        return UnitInterpreterValue.UNIT;
    }

    private static Environment buildEnvironment(
        final Environment environment,
        final MethodNode method,
        Arguments arguments) {
        
        val stackFrame = new StackFrameBuilder();
        for (int index = 0; index < method.getArguments().size(); index++) {
            stackFrame.declare(method.getArguments().get(index), arguments.get(index));
        }
        
        findDeclarations(method.getBody()).forEach(declaration ->
            stackFrame.declare(declaration));
        
        return environment.withStackFrame(stackFrame.build());
    }
    
    private static Stream<VariableNode> findDeclarations(List<StatementNode> body) {
        return body.stream()
            .flatMap(statement -> statement.accept(new StatementNodeVisitor<Stream<VariableNode>>() {
                @Override
                public Stream<VariableNode> visit(ReturnNode returnNode) {
                    return Stream.empty();
                }
    
                @Override
                public Stream<VariableNode> visit(ExpressionStatementNode expressionStatement) {
                    return Stream.empty();
                }
    
                @Override
                public Stream<VariableNode> visit(LocalVariableDeclarationNode localVariableDeclaration) {
                    return Stream.of(localVariableDeclaration);
                }
            }));
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

    @Override
    public Optional<InterpreterValue> visit(LocalVariableDeclarationNode localVariableDeclaration) {
        val value = eval(environment, localVariableDeclaration.getInitialValue());
        environment.put(localVariableDeclaration, value);
        return Optional.empty();
    }
}
