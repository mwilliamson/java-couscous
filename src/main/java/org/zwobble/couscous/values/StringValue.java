package org.zwobble.couscous.values;

import lombok.Value;
import lombok.val;

@Value
public class StringValue implements InterpreterValue {
    private static final ConcreteType<?> TYPE = ConcreteType.<StringValue>builder()
        .method("length", (receiver, arguments) -> new IntegerValue(receiver.value.length()))
        
        .method("substring", (receiver, arguments) -> {
            val startIndex = arguments.pop(IntegerValue.class);
            val endIndex = arguments.pop(IntegerValue.class);
            return new StringValue(receiver.value.substring(startIndex.getValue(), endIndex.getValue()));
        })
        
        .build();
    
    String value;

    @Override
    public ConcreteType<?> getType() {
        return TYPE;
    }
}
