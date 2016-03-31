package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.AssignableExpressionNodeVisitor;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.interpreter.errors.ConditionMustBeBoolean;
import org.zwobble.couscous.interpreter.errors.InvalidCast;
import org.zwobble.couscous.interpreter.values.*;
import org.zwobble.couscous.types.ParameterizedType;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class Evaluator implements ExpressionNodeMapper<InterpreterValue> {
    public static InterpreterValue eval(Environment environment, ExpressionNode expression) {
        return new Evaluator(environment).eval(expression);
    }

    public static boolean evalCondition(Environment environment, ExpressionNode expression) {
        return new Evaluator(environment).evalCondition(expression);
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
    public InterpreterValue visit(ArrayNode array) {
        return new ArrayInterpreterValue(
            array.getElementType(),
            eagerMap(array.getElements(), this::eval));
    }

    @Override
    public InterpreterValue visit(AssignmentNode assignment) {
        InterpreterValue value = eval(assignment.getValue());
        assignment.getTarget().accept(new AssignableExpressionNodeVisitor(){
            @Override
            public void visit(FieldAccessNode fieldAccess) {
                ReceiverValue left = evalReceiver(fieldAccess.getLeft());
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
        boolean condition = evalCondition(ternaryConditional.getCondition());
        ExpressionNode branch = condition ? ternaryConditional.getIfTrue() : ternaryConditional.getIfFalse();
        return eval(branch);
    }

    private boolean evalCondition(ExpressionNode condition) {
        InterpreterValue value = eval(condition);
        if (!(value instanceof BooleanInterpreterValue)) {
            throw new ConditionMustBeBoolean(value);
        }
        return ((BooleanInterpreterValue)value).getValue();
    }

    @Override
    public InterpreterValue visit(MethodCallNode methodCall) {
        List<InterpreterValue> arguments = evalArguments(methodCall.getArguments());
        MethodSignature signature = methodCall.signature();

        return evalReceiver(methodCall.getReceiver())
            .callMethod(environment, signature, arguments);
    }
    
    @Override
    public InterpreterValue visit(ConstructorCallNode call) {
        StaticReceiverValue clazz = environment.findClass(erasure(call.getType()));
        List<InterpreterValue> arguments = evalArguments(call.getArguments());
        Optional<List<Type>> typeParameters = tryCast(ParameterizedType.class, call.getType())
            .map(type -> type.getParameters());
        return clazz.callConstructor(environment, typeParameters, arguments);
    }

    @Override
    public InterpreterValue visit(OperationNode operation) {
        return eval(operation.desugar());
    }

    @Override
    public InterpreterValue visit(FieldAccessNode fieldAccess) {
        ReceiverValue left = evalReceiver(fieldAccess.getLeft());
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
            throw new InvalidCast(cast.getType(), value.getType().getType());
        }
        return value;
    }

    private static boolean isIntegerBox(TypeCoercionNode typeCoercion) {
        return typeCoercion.getExpression().getType().equals(Types.INT) &&
            !typeCoercion.getType().equals(Types.INT);
    }

    private static boolean isIntegerUnbox(TypeCoercionNode typeCoercion) {
        return !typeCoercion.getExpression().getType().equals(Types.INT) &&
            typeCoercion.getType().equals(Types.INT);
    }

    private static boolean isBooleanBox(TypeCoercionNode typeCoercion) {
        return typeCoercion.getExpression().getType().equals(Types.BOOLEAN) &&
            !typeCoercion.getType().equals(Types.BOOLEAN);
    }

    private static boolean isBooleanUnbox(TypeCoercionNode typeCoercion) {
        return !typeCoercion.getExpression().getType().equals(Types.BOOLEAN) &&
            typeCoercion.getType().equals(Types.BOOLEAN);
    }

    private List<InterpreterValue> evalArguments(List<? extends ExpressionNode> arguments) {
        return arguments.stream()
            .map(argument -> eval(argument))
            .collect(Collectors.toList());
    }

    private ReceiverValue evalReceiver(Receiver receiver) {
        return receiver.accept(new Receiver.Mapper<ReceiverValue>() {
            @Override
            public ReceiverValue visit(ExpressionNode receiver) {
                return new InstanceReceiverValue(eval(receiver));
            }

            @Override
            public ReceiverValue visit(ScalarType receiver) {
                return environment.findClass(receiver);
            }
        });
    }
}