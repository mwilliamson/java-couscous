package org.zwobble.couscous.interpreter.values;

import java.util.Optional;

import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;

import lombok.Value;
import lombok.val;

@Value
public class StringInterpreterValue implements InterpreterValue {
    public static final ConcreteType<?> TYPE = ConcreteType.<StringInterpreterValue>builder(StringValue.REF)
        .method("length", asList(),
            (receiver, arguments) -> new IntegerInterpreterValue(receiver.value.length()))
        
        .method("substring", asList(IntegerValue.REF, IntegerValue.REF),
            (receiver, arguments) -> {
                val startIndex = (IntegerInterpreterValue)arguments.get(0);
                val endIndex = (IntegerInterpreterValue)arguments.get(1);
                return new StringInterpreterValue(receiver.value.substring(startIndex.getValue(), endIndex.getValue()));
            })
        
        .build();
    
    String value;

    @Override
    public ConcreteType<?> getType() {
        return TYPE;
    }

    @Override
    public Optional<PrimitiveValue> toPrimitiveValue() {
        return Optional.of(PrimitiveValues.value(value));
    }
}
