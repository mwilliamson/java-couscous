package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.Operator;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.zwobble.couscous.util.ExtraLists.list;

public final class IntegerInterpreterValue implements InterpreterValue {
    public static final InterpreterType TYPE = IntrinsicInterpreterType.builder(IntegerInterpreterValue.class, Types.INT)
        .method(Operator.ADD.getSymbol(), list(Types.INT), Types.INT,
            infixReturningInteger((left, right) -> left + right))
        .method(Operator.SUBTRACT.getSymbol(), list(Types.INT), Types.INT,
            infixReturningInteger((left, right) -> left - right))
        .method(Operator.MULTIPLY.getSymbol(), list(Types.INT), Types.INT,
            infixReturningInteger((left, right) -> left * right))
        .method(Operator.DIVIDE.getSymbol(), list(Types.INT), Types.INT,
            infixReturningInteger((left, right) -> left / right))
        .method(Operator.MOD.getSymbol(), list(Types.INT), Types.INT,
            infixReturningInteger((left, right) -> left % right))
        .method(Operator.EQUALS.getSymbol(), list(Types.INT), Types.BOOLEAN,
            (environment, arguments) -> {
                IntegerInterpreterValue right = (IntegerInterpreterValue)arguments.getPositionalArguments().get(0);
                return integerEquals(arguments, right);
            })
        .method(Operator.NOT_EQUALS.getSymbol(), list(Types.INT), Types.BOOLEAN,
            infixReturningBoolean((left, right) -> !left.equals(right)))
        .method(Operator.GREATER_THAN.getSymbol(), list(Types.INT), Types.BOOLEAN,
            infixReturningBoolean((left, right) -> left > right))
        .method(Operator.GREATER_THAN_OR_EQUAL.getSymbol(), list(Types.INT), Types.BOOLEAN,
            infixReturningBoolean((left, right) -> left >= right))
        .method(Operator.LESS_THAN.getSymbol(), list(Types.INT), Types.BOOLEAN,
            infixReturningBoolean((left, right) -> left < right))
        .method(Operator.LESS_THAN_OR_EQUAL.getSymbol(), list(Types.INT), Types.BOOLEAN,
            infixReturningBoolean((left, right) -> left <= right))
        .build();

    private static BooleanInterpreterValue integerEquals(MethodCallArguments<IntegerInterpreterValue> arguments, IntegerInterpreterValue right) {
        return BooleanInterpreterValue.of(arguments.getReceiver().getValue() == right.getValue());
    }

    private static
            BiFunction<Environment, MethodCallArguments<IntegerInterpreterValue>, InterpreterValue>
            infixReturningInteger(BiFunction<Integer, Integer, Integer> func) {
        return infix((left, right) -> new IntegerInterpreterValue(func.apply(left, right)));
    }
    
    private static
            BiFunction<Environment, MethodCallArguments<IntegerInterpreterValue>, InterpreterValue>
            infixReturningBoolean(BiFunction<Integer, Integer, Boolean> func) {
        return infix((left, right) -> BooleanInterpreterValue.of(func.apply(left, right)));
    }
    
    private static
            BiFunction<Environment, MethodCallArguments<IntegerInterpreterValue>, InterpreterValue>
            infix(BiFunction<Integer, Integer, InterpreterValue> func) {
        return (environment, arguments) -> {
            IntegerInterpreterValue left = arguments.getReceiver();
            IntegerInterpreterValue right = (IntegerInterpreterValue) arguments.get(0);
            return func.apply(left.getValue(), right.getValue());
        };
    }
    
    private final int value;
    
    @Override
    public InterpreterType getType() {
        return TYPE;
    }
    
    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.of(PrimitiveValues.value(value));
    }
    
    @Override
    public InterpreterValue getField(String fieldName) {
        throw new NoSuchField(fieldName);
    }
    
    @Override
    public void setField(String fieldName, InterpreterValue value) {
        throw new NoSuchField(fieldName);
    }
    
    public IntegerInterpreterValue(final int value) {
        this.value = value;
    }
    
    public int getValue() {
        return this.value;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof IntegerInterpreterValue)) return false;
        final IntegerInterpreterValue other = (IntegerInterpreterValue)o;
        if (this.getValue() != other.getValue()) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getValue();
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "IntegerInterpreterValue(value=" + this.getValue() + ")";
    }
}