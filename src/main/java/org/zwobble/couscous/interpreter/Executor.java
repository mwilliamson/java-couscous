package org.zwobble.couscous.interpreter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;
import org.zwobble.couscous.interpreter.values.BooleanInterpreterValue;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.UnitInterpreterValue;

import static org.zwobble.couscous.interpreter.Evaluator.eval;

public class Executor implements StatementNodeMapper<Optional<InterpreterValue>> {
    public static InterpreterValue callMethod(Environment environment, CallableNode method, Optional<InterpreterValue> thisValue, PositionalArguments arguments) {
        final org.zwobble.couscous.interpreter.Environment innerEnvironment = buildEnvironment(environment, method, thisValue, arguments);
        for (final org.zwobble.couscous.ast.StatementNode statement : method.getBody()) {
            final java.util.Optional<org.zwobble.couscous.interpreter.values.InterpreterValue> result = exec(innerEnvironment, statement);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return UnitInterpreterValue.UNIT;
    }
    
    private static Environment buildEnvironment(final Environment environment, final CallableNode method, Optional<InterpreterValue> thisValue, PositionalArguments arguments) {
        final org.zwobble.couscous.interpreter.StackFrameBuilder stackFrame = new StackFrameBuilder();
        for (int index = 0; index < method.getArguments().size(); index++) {
            stackFrame.declare(method.getArguments().get(index), arguments.get(index));
        }
        findDeclarations(method.getBody()).forEach(declaration -> stackFrame.declare(declaration));
        return environment.withStackFrame(thisValue, stackFrame.build());
    }
    
    private static Stream<VariableNode> findDeclarations(List<StatementNode> body) {
        return body.stream().flatMap(statement -> statement.accept(new StatementNodeMapper<Stream<VariableNode>>(){
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

            @Override
            public Stream<VariableNode> visit(IfStatementNode ifStatement) {
                // TODO: Handle declarations in branches
                return Stream.empty();
            }

            @Override
            public Stream<VariableNode> visit(WhileNode whileLoop) {
                // TODO: Handle declarations in branches
                return Stream.empty();
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
        final org.zwobble.couscous.interpreter.values.InterpreterValue value = eval(environment, localVariableDeclaration.getInitialValue());
        environment.put(localVariableDeclaration, value);
        return Optional.empty();
    }

    @Override
    public Optional<InterpreterValue> visit(IfStatementNode ifStatement) {
        BooleanInterpreterValue condition = (BooleanInterpreterValue) eval(environment, ifStatement.getCondition());
        List<StatementNode> body = condition.getValue()
            ? ifStatement.getTrueBranch()
            : ifStatement.getFalseBranch();
        return exec(body);
    }

    @Override
    public Optional<InterpreterValue> visit(WhileNode whileLoop) {
        throw new UnsupportedOperationException();
    }

    private Optional<InterpreterValue> exec(List<StatementNode> statements) {
        for (StatementNode statement : statements) {
            Optional<InterpreterValue> result = exec(environment, statement);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
}