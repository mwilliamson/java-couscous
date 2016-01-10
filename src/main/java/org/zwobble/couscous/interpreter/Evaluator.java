package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.AssignableExpressionNodeVisitor;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.interpreter.errors.ConditionMustBeBoolean;
import org.zwobble.couscous.interpreter.errors.InvalidCast;
import org.zwobble.couscous.interpreter.values.*;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;

import java.util.List;
import java.util.stream.Collectors;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;

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
        List<InterpreterValue> arguments = evalArguments(methodCall.getArguments());
        MethodSignature signature = new MethodSignature(
            methodCall.getMethodName(),
            eagerMap(methodCall.getArguments(), argument -> argument.getType()));

        return methodCall.getReceiver().accept(new Receiver.Mapper<InterpreterValue>() {
            @Override
            public InterpreterValue visit(ExpressionNode receiverExpression) {
                InterpreterValue receiver = eval(receiverExpression);
                ConcreteType type = receiver.getType();
                return type.callMethod(environment, receiver, signature, arguments);
            }

            @Override
            public InterpreterValue visit(TypeName receiver) {
                ConcreteType clazz = environment.findClass(receiver);
                return clazz.callStaticMethod(environment, signature, arguments);
            }
        });
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

    @Override
    public InterpreterValue visit(TypeCoercionNode typeCoercion) {
        // TODO: check that the type coercion is valid
        // TODO: boxing booleans
        InterpreterValue value = eval(typeCoercion.getExpression());
        if (isIntegerBox(typeCoercion)) {
            return BoxedIntegerInterpreterValue.of(((IntegerInterpreterValue)value));
        } else if (isIntegerUnbox(typeCoercion)) {
            return value.getField("value");
        } else {
            return value;
        }
    }

    @Override
    public InterpreterValue visit(CastNode cast) {
        InterpreterValue value = eval(cast.getExpression());
        if (!InterpreterTypes.isSubType(cast.getType(), value.getType())) {
            throw new InvalidCast(cast.getType(), value.getType().getName());
        }
        return value;
    }

    private static boolean isIntegerBox(TypeCoercionNode typeCoercion) {
        return typeCoercion.getExpression().getType().equals(IntegerValue.REF) &&
            !typeCoercion.getType().equals(IntegerValue.REF);
    }

    private static boolean isIntegerUnbox(TypeCoercionNode typeCoercion) {
        return !typeCoercion.getExpression().getType().equals(IntegerValue.REF) &&
            typeCoercion.getType().equals(IntegerValue.REF);
    }

    private static boolean isBooleanBox(TypeCoercionNode typeCoercion) {
        return typeCoercion.getExpression().getType().equals(BooleanValue.REF) &&
            !typeCoercion.getType().equals(BooleanValue.REF);
    }

    private static boolean isBooleanUnbox(TypeCoercionNode typeCoercion) {
        return !typeCoercion.getExpression().getType().equals(BooleanValue.REF) &&
            typeCoercion.getType().equals(BooleanValue.REF);
    }

    private List<InterpreterValue> evalArguments(List<? extends ExpressionNode> arguments) {
        return arguments.stream()
            .map(argument -> eval(argument))
            .collect(Collectors.toList());
    }
}