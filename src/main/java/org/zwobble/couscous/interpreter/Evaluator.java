package org.zwobble.couscous.interpreter;

import java.util.List;
import java.util.stream.Collectors;

import org.zwobble.couscous.ast.Assignment;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.VariableReferenceNode;
import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;
import org.zwobble.couscous.interpreter.values.BooleanValue;
import org.zwobble.couscous.interpreter.values.InterpreterValue;

import lombok.val;

public class Evaluator implements ExpressionNodeVisitor<InterpreterValue> {
    public static InterpreterValue eval(Environment environment, ExpressionNode expression) {
        return new Evaluator(environment).eval(expression);
    }
    
    private final Environment environment;

    private Evaluator(Environment environment) {
        this.environment = environment;
    }
    
    public InterpreterValue eval(ExpressionNode expression) {
        return expression.accept(this);
    }

    @Override
    public InterpreterValue visit(LiteralNode literal) {
        return literal.getValue();
    }

    @Override
    public InterpreterValue visit(VariableReferenceNode variableReference) {
        return environment.get(variableReference.getReferentId());
    }

    @Override
    public InterpreterValue visit(Assignment assignment) {
        val value = eval(assignment.getValue());
        environment.put(assignment.getTarget().getReferentId(), value);
        return value;
    }

    @Override
    public InterpreterValue visit(TernaryConditionalNode ternaryConditional) {
        val condition = eval(ternaryConditional.getCondition());
        if (!(condition instanceof BooleanValue)) {
            throw new ConditionMustBeBoolean(condition);
        }
        val branch = ((BooleanValue)condition).getValue()
            ? ternaryConditional.getIfTrue()
            : ternaryConditional.getIfFalse();
        return eval(branch);
    }

    @Override
    public InterpreterValue visit(MethodCallNode methodCall) {
        val receiver = eval(methodCall.getReceiver());
        val type = receiver.getType();
        val arguments = evalArguments(methodCall.getArguments());
        return type.callMethod(receiver, methodCall.getMethodName(), arguments);
    }

    private List<InterpreterValue> evalArguments(List<ExpressionNode> arguments) {
        return arguments
            .stream()
            .map(argument -> eval(argument))
            .collect(Collectors.toList());
    }

    @Override
    public InterpreterValue visit(StaticMethodCallNode staticMethodCall) {
        val clazz = environment.findClass(staticMethodCall.getClassName());
        val arguments = evalArguments(staticMethodCall.getArguments());
        return clazz.callStaticMethod(environment, staticMethodCall.getMethodName(), arguments);
    }
}
