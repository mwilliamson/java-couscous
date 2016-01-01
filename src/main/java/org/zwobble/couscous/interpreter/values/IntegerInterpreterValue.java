package org.zwobble.couscous.interpreter.values;

import java.util.Optional;
import java.util.function.BiFunction;

import org.zwobble.couscous.ast.Operator;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.NoSuchField;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

import static java.util.Arrays.asList;

public final class IntegerInterpreterValue implements InterpreterValue {
    public static final ConcreteType TYPE = ConcreteType.builder(IntegerInterpreterValue.class, IntegerValue.REF)
        .method(Operator.ADD.getMethodName(), asList(IntegerValue.REF),
            infixReturningInteger((left, right) -> left + right))
        .method(Operator.SUBTRACT.getMethodName(), asList(IntegerValue.REF),
            infixReturningInteger((left, right) -> left - right))
        .method(Operator.MULTIPLY.getMethodName(), asList(IntegerValue.REF),
            infixReturningInteger((left, right) -> left * right))
        .method(Operator.DIVIDE.getMethodName(), asList(IntegerValue.REF),
            infixReturningInteger((left, right) -> left / right))
        .method(Operator.MOD.getMethodName(), asList(IntegerValue.REF),
            infixReturningInteger((left, right) -> left % right))
        .method(Operator.EQUALS.getMethodName(), asList(ObjectValues.OBJECT),
            (environment, arguments) -> {
                InterpreterValue right = arguments.getPositionalArguments().get(0);
                if (right instanceof IntegerInterpreterValue) {
                    return new BooleanInterpreterValue(arguments.getReceiver().getValue() == ((IntegerInterpreterValue)right).getValue());
                } else {
                    return new BooleanInterpreterValue(false);
                }
            })
        .method(Operator.NOT_EQUALS.getMethodName(), asList(IntegerValue.REF),
            infixReturningBoolean((left, right) -> !left.equals(right)))
        .method(Operator.GREATER_THAN.getMethodName(), asList(IntegerValue.REF),
            infixReturningBoolean((left, right) -> left > right))
        .method(Operator.GREATER_THAN_OR_EQUAL.getMethodName(), asList(IntegerValue.REF),
            infixReturningBoolean((left, right) -> left >= right))
        .method(Operator.LESS_THAN.getMethodName(), asList(IntegerValue.REF),
            infixReturningBoolean((left, right) -> left < right))
        .method(Operator.LESS_THAN_OR_EQUAL.getMethodName(), asList(IntegerValue.REF),
            infixReturningBoolean((left, right) -> left <= right))
        .build();
    
    private static
            BiFunction<Environment, MethodCallArguments<IntegerInterpreterValue>, InterpreterValue>
            infixReturningInteger(BiFunction<Integer, Integer, Integer> func) {
        return infix((left, right) -> new IntegerInterpreterValue(func.apply(left, right)));
    }
    
    private static
            BiFunction<Environment, MethodCallArguments<IntegerInterpreterValue>, InterpreterValue>
            infixReturningBoolean(BiFunction<Integer, Integer, Boolean> func) {
        return infix((left, right) -> new BooleanInterpreterValue(func.apply(left, right)));
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
    public ConcreteType getType() {
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

    public InterpreterValue box() {
        ObjectInterpreterValue box = new ObjectInterpreterValue(BoxedIntegerInterpreterValue.TYPE);
        box.setField("value", this);
        return box;
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