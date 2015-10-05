package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.values.TypeReference;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.values.TypeReference.typeRef;

import lombok.Value;
import lombok.val;

@Value
public class StringValue implements InterpreterValue {
    public static final TypeReference REF = typeRef("java.lang.String");
    
    public static final ConcreteType<?> TYPE = ConcreteType.<StringValue>builder(REF)
        .method("length", asList(),
            (receiver, arguments) -> new IntegerValue(receiver.value.length()))
        
        .method("substring", asList(IntegerValue.REF, IntegerValue.REF),
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
