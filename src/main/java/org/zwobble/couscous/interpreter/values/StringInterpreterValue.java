package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.Operator;
import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.*;

import java.util.Optional;

import static org.zwobble.couscous.util.ExtraLists.list;

public final class StringInterpreterValue implements InterpreterValue {
    public static final InterpreterType TYPE = IntrinsicInterpreterType.builder(StringInterpreterValue.class, Types.STRING)
        .method("length", list(), Types.INT, (environment, arguments) ->
            new IntegerInterpreterValue(arguments.getReceiver().value.length()))

        .method("substring", list(Types.INT, Types.INT), Types.STRING, (environment, arguments) -> {
            IntegerInterpreterValue startIndex = (IntegerInterpreterValue)arguments.get(0);
            IntegerInterpreterValue endIndex = (IntegerInterpreterValue)arguments.get(1);
            return of(arguments.getReceiver().value.substring(startIndex.getValue(), endIndex.getValue()));
        })

        .method(Operator.ADD.getSymbol(), list(Types.STRING), Types.STRING, (environment, arguments) -> {
            StringInterpreterValue right = (StringInterpreterValue)arguments.get(0);
            return of(arguments.getReceiver().value + right.value);
        })

        .method("toLowerCase", list(), Types.STRING, (environment, arguments) ->
            of(arguments.getReceiver().value.toLowerCase()))

        .method("equals", list(Types.OBJECT), Types.BOOLEAN, (environment, arguments) -> {
            InterpreterValue right = arguments.get(0);
            if (right instanceof StringInterpreterValue) {
                return BooleanInterpreterValue.of(arguments.getReceiver().value.equals(((StringInterpreterValue)right).value));
            } else {
                return InterpreterValues.FALSE;
            }
        })

        .build();

    private final String value;

    private StringInterpreterValue(final String value) {
        this.value = value;
    }

    public static StringInterpreterValue of(final String value) {
        return new StringInterpreterValue(value);
    }

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
    
    public String getValue() {
        return this.value;
    }
    
    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof StringInterpreterValue)) return false;
        final StringInterpreterValue other = (StringInterpreterValue)o;
        final java.lang.Object this$value = this.getValue();
        final java.lang.Object other$value = other.getValue();
        if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "StringInterpreterValue(value=" + this.getValue() + ")";
    }
}