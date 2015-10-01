package org.zwobble.couscous.values;

import static java.util.Arrays.asList;

import lombok.Value;
import lombok.val;

@Value
public class StringValue implements InterpreterValue {
    public static final ConcreteType<?> TYPE = ConcreteType.<StringValue>builder("String")
        .method("length", () -> asList(),
            (receiver, arguments) -> new IntegerValue(receiver.value.length()))
        
        .method("substring", () -> asList(IntegerValue.TYPE, IntegerValue.TYPE),
            (receiver, arguments) -> {
                val startIndex = (IntegerValue)arguments.get(0);
                val endIndex = (IntegerValue)arguments.get(1);
                return new StringValue(receiver.value.substring(startIndex.getValue(), endIndex.getValue()));
            })
        
        .build();
    
    String value;

    @Override
    public ConcreteType<?> getType() {
        return TYPE;
    }
}
