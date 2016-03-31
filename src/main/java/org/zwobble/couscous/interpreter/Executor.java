package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeMapperWithDefault;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.UnitInterpreterValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.zwobble.couscous.ast.structure.NodeStructure.descendantNodesAndSelf;
import static org.zwobble.couscous.interpreter.Evaluator.eval;
import static org.zwobble.couscous.interpreter.Evaluator.evalCondition;
import static org.zwobble.couscous.util.ExtraIterables.forEach;

public class Executor implements StatementNodeMapper<Optional<InterpreterValue>> {
    public static InterpreterValue callMethod(
        Environment environment,
        MethodNode method,
        Optional<InterpreterValue> thisValue,
        Arguments actualArguments)
    {
        List<StatementNode> body = method.getBody().orElseThrow(() -> new RuntimeException("Cannot call abstract method"));
        return callMethod(environment, method.getArguments(), body, thisValue, actualArguments);
    }

    public static InterpreterValue callConstructor(
        Environment environment,
        ConstructorNode constructor,
        InterpreterValue thisValue,
        Arguments actualArguments)
    {
        return callMethod(environment, constructor.getArguments(), constructor.getBody(), Optional.of(thisValue), actualArguments);
    }

    private static InterpreterValue callMethod(
        Environment environment,
        List<FormalArgumentNode> formalArguments,
        List<StatementNode> statements,
        Optional<InterpreterValue> thisValue,
        Arguments actualArguments)
    {
        Environment innerEnvironment = buildEnvironment(environment, formalArguments, statements, thisValue, actualArguments);
        return exec(innerEnvironment, statements)
            .orElse(UnitInterpreterValue.UNIT);
    }

    private static Environment buildEnvironment(
        Environment environment,
        List<FormalArgumentNode> formalArguments,
        List<StatementNode> statements,
        Optional<InterpreterValue> thisValue,
        Arguments arguments)
    {
        StackFrameBuilder stackFrame = new StackFrameBuilder();
        forEach(formalArguments, arguments.getValues(), stackFrame::declare);
        findDeclarations(statements).forEach(declaration -> stackFrame.declare(declaration));
        return environment.withStackFrame(thisValue, stackFrame.build());
    }

    private static Stream<VariableNode> findDeclarations(List<StatementNode> body) {
        return body.stream()
            .flatMap(statement -> descendantNodesAndSelf(statement, node -> node instanceof StatementNode))
            .flatMap(node -> node.accept(new NodeMapperWithDefault<Stream<VariableNode>>(Stream.empty()) {
                @Override
                public Stream<VariableNode> visit(LocalVariableDeclarationNode localVariableDeclaration) {
                    return Stream.of(localVariableDeclaration);
                }
            }));
    }

    public static Optional<InterpreterValue> exec(Environment environment, Iterable<StatementNode> statements) {
        for (StatementNode statement : statements) {
            Optional<InterpreterValue> result = exec(environment, statement);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
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
        List<StatementNode> body = evalCondition(environment, ifStatement.getCondition())
            ? ifStatement.getTrueBranch()
            : ifStatement.getFalseBranch();
        return exec(body);
    }

    @Override
    public Optional<InterpreterValue> visit(WhileNode whileLoop) {
        while (evalCondition(environment, whileLoop.getCondition())) {
            Optional<InterpreterValue> result = exec(whileLoop.getBody());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
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