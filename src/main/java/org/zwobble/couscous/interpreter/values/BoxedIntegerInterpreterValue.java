package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;

import lombok.val;

public class BoxedIntegerInterpreterValue {
    public static final ConcreteType TYPE = ConcreteType.classBuilder("java.lang.Integer")
        .staticMethod("parseInt", asList(StringValue.REF),
            (environment, arguments) -> {
                val value = (StringInterpreterValue)arguments.get(0);
                return new IntegerInterpreterValue(Integer.parseInt(value.getValue()));
            })
        
        .build();
}
