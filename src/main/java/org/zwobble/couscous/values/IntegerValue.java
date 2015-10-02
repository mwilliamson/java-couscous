package org.zwobble.couscous.values;

import static java.util.Arrays.asList;

import lombok.Value;
import lombok.val;

@Value
public class IntegerValue implements InterpreterValue {
    public static final ConcreteType<?> TYPE = ConcreteType.<IntegerValue>builder("java.lang.Integer")
        .staticMethod("parseInt", () -> asList(StringValue.TYPE),
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
