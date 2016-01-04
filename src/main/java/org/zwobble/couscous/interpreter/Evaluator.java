package org.zwobble.couscous.interpreter;

import java.util.List;
import java.util.stream.Collectors;
import org.zwobble.couscous.ast.AssignmentNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.FieldAccessNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.ThisReferenceNode;
import org.zwobble.couscous.ast.VariableReferenceNode;
import org.zwobble.couscous.ast.visitors.AssignableExpressionNodeVisitor;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.interpreter.values.BooleanInterpreterValue;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.InterpreterValues;

public class Evaluator implements ExpressionNodeMapper<InterpreterValue> {
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
        return InterpreterValues.value(literal.getValue());
    }
    
    @Override
    public InterpreterValue visit(VariableReferenceNode variableReference) {
        return environment.get(variableReference.getReferentId());
    }
    
    @Override
    public InterpreterValue visit(ThisReferenceNode reference) {
        InterpreterValue thisValue = environment.getThis().get();
        // TODO: add a test for this
        InterpreterTypes.checkIsInstance(reference.getType(), thisValue);
        return thisValue;
    }
    
    @Override
    public InterpreterValue visit(AssignmentNode assignment) {
        InterpreterValue value = eval(assignment.getValue());
        assignment.getTarget().accept(new AssignableExpressionNodeVisitor(){
            @Override
            public void visit(FieldAccessNode fieldAccess) {
                InterpreterValue left = eval(fieldAccess.getLeft());
                left.setField(fieldAccess.getFieldName(), value);
            }
            
            @Override
            public void visit(VariableReferenceNode reference) {
                environment.put(reference.getReferentId(), value);
            }
        });
        return value;
    }
    
    @Override
    public InterpreterValue visit(TernaryConditionalNode ternaryConditional) {
        InterpreterValue condition = eval(ternaryConditional.getCondition());
        if (!(condition instanceof BooleanInterpreterValue)) {
            throw new ConditionMustBeBoolean(condition);
        }
        final org.zwobble.couscous.ast.ExpressionNode branch = ((BooleanInterpreterValue)condition).getValue() ? ternaryConditional.getIfTrue() : ternaryConditional.getIfFalse();
        return eval(branch);
    }
    
    @Override
    public InterpreterValue visit(MethodCallNode methodCall) {
        InterpreterValue receiver = eval(methodCall.getReceiver());
        ConcreteType type = receiver.getType();
        List<InterpreterValue> arguments = evalArguments(methodCall.getArguments());
        return type.callMethod(environment, receiver, methodCall.getMethodName(), arguments);
    }
    
    @Override
    public InterpreterValue visit(StaticMethodCallNode staticMethodCall) {
        ConcreteType clazz = environment.findClass(staticMethodCall.getClassName());
        List<InterpreterValue> arguments = evalArguments(staticMethodCall.getArguments());
        return clazz.callStaticMethod(environment, staticMethodCall.getMethodName(), arguments);
    }
    
    @Override
    public InterpreterValue visit(ConstructorCallNode call) {
        ConcreteType clazz = environment.findClass(call.getType());
        List<InterpreterValue> arguments = evalArguments(call.getArguments());
        return clazz.callConstructor(environment, arguments);
    }
    
    @Override
    public InterpreterValue visit(FieldAccessNode fieldAccess) {
        InterpreterValue left = eval(fieldAccess.getLeft());
        return left.getField(fieldAccess.getFieldName());
    }
    
    private List<InterpreterValue> evalArguments(List<? extends ExpressionNode> arguments) {
        return arguments.stream()
            .map(argument -> eval(argument))
            .collect(Collectors.toList());
    }
}