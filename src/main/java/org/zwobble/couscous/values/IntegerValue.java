package org.zwobble.couscous.values;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.values.TypeReference.typeRef;

import lombok.Value;
import lombok.val;

@Value
public class IntegerValue implements InterpreterValue {
    public static final TypeReference REF = typeRef("java.lang.Integer");
    
    public static final ConcreteType<?> TYPE = ConcreteType.<IntegerValue>builder(REF)
        .staticMethod("parseInt", asList(StringValue.REF),
            (environment, arguments) -> {
                val value = (StringValue)arguments.get(0);
                return new IntegerValue(Integer.parseInt(value.getValue()));
            })
        
        .build();
    
    int value;

    @Override
    public ConcreteType<?> getType() {
        return TYPE;
    }
}
