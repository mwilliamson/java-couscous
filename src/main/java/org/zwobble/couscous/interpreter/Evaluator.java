package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.DynamicNodeMapper;
import org.zwobble.couscous.ast.visitors.DynamicNodeVisitor;
import org.zwobble.couscous.interpreter.errors.ConditionMustBeBoolean;
import org.zwobble.couscous.interpreter.errors.InvalidCast;
import org.zwobble.couscous.interpreter.values.*;
import org.zwobble.couscous.types.ParameterizedType;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class Evaluator {
    public static InterpreterValue eval(Environment environment, ExpressionNode expression) {
        return new Evaluator(environment).eval(expression);
    }

    public static boolean evalCondition(Environment environment, ExpressionNode expression) {
        return new Evaluator(environment).evalCondition(expression);
    }

    private static final BiFunction<Node, Evaluator, InterpreterValue> EVAL = DynamicNodeMapper.visitor(Evaluator.class, "visit");
    
    private final Environment environment;
    
    private Evaluator(Environment environment) {
        this.environment = environment;
    }
    
    public InterpreterValue eval(ExpressionNode expression) {
        return EVAL.apply(expression, this);
    }
    
    public InterpreterValue visit(LiteralNode literal) {
        return InterpreterValues.value(literal.getValue());
    }
    
    public InterpreterValue visit(VariableReferenceNode variableReference) {
        return environment.get(variableReference.getReferentId());
    }
    
    public InterpreterValue visit(ThisReferenceNode reference) {
        InterpreterValue thisValue = environment.getThis().get();
        // TODO: add a test for this
        //InterpreterTypes.checkIsInstance(reference.getType(), thisValue);
        return thisValue;
    }

    public InterpreterValue visit(ArrayNode array) {
        return new ArrayInterpreterValue(
            array.getElementType(),
            eagerMap(array.getElements(), this::eval));
    }

    public InterpreterValue visit(AssignmentNode assignment) {
        InterpreterValue value = eval(assignment.getValue());
        AssignableExpressionVisitor.visit.accept(assignment.getTarget(), new AssignableExpressionVisitor() {
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

    public interface AssignableExpressionVisitor {
        BiConsumer<Node, AssignableExpressionVisitor> visit = DynamicNodeVisitor.visitor(AssignableExpressionVisitor.class);

        void visit(VariableReferenceNode node);
        void visit(FieldAccessNode node);
    }
    
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

    public InterpreterValue visit(MethodCallNode methodCall) {
        List<InterpreterValue> arguments = evalArguments(methodCall.getArguments());
        MethodSignature signature = methodCall.signature();

        return evalReceiver(methodCall.getReceiver())
            .callMethod(environment, signature.generic(), new Arguments(list(), arguments));
    }
    
    public InterpreterValue visit(ConstructorCallNode call) {
        StaticReceiverValue clazz = environment.findClass(erasure(call.getType()));
        List<InterpreterValue> arguments = evalArguments(call.getArguments());
        List<Type> typeParameters = tryCast(ParameterizedType.class, call.getType())
            .map(type -> type.getParameters())
            .orElse(list());
        return clazz.callConstructor(environment, new Arguments(typeParameters, arguments));
    }

    public InterpreterValue visit(OperationNode operation) {
        return eval(operation.desugar());
    }

    public InterpreterValue visit(FieldAccessNode fieldAccess) {
        ReceiverValue left = evalReceiver(fieldAccess.getLeft());
        return left.getField(fieldAccess.getFieldName());
    }

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
