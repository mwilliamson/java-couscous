package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.NoSuchField;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;
import org.zwobble.couscous.values.StringValue;

import java.util.Optional;

import static org.zwobble.couscous.util.ExtraLists.list;

public final class StringInterpreterValue implements InterpreterValue {
    public static final ConcreteType TYPE = ConcreteType.builder(StringInterpreterValue.class, StringValue.REF)
        .method("length", list(), (environment, arguments) ->
            new IntegerInterpreterValue(arguments.getReceiver().value.length()))

        .method("substring", list(IntegerValue.REF, IntegerValue.REF), (environment, arguments) -> {
            IntegerInterpreterValue startIndex = (IntegerInterpreterValue)arguments.get(0);
            IntegerInterpreterValue endIndex = (IntegerInterpreterValue)arguments.get(1);
            return new StringInterpreterValue(arguments.getReceiver().value.substring(startIndex.getValue(), endIndex.getValue()));
        })

        .method("add", list(StringValue.REF), (environment, arguments) -> {
            StringInterpreterValue right = (StringInterpreterValue)arguments.get(0);
            return new StringInterpreterValue(arguments.getReceiver().value + right.value);
        })

        .build();

    private final String value;
    
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
    
    public StringInterpreterValue(final String value) {
        this.value = value;
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