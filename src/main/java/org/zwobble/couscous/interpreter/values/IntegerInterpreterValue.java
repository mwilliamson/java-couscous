package org.zwobble.couscous.interpreter.values;

import java.util.Optional;

import org.zwobble.couscous.interpreter.NoSuchField;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;

import lombok.Value;
import lombok.val;

@Value
public class IntegerInterpreterValue implements InterpreterValue {
    public static final ConcreteType TYPE = ConcreteType.builder(IntegerInterpreterValue.class, IntegerValue.REF)
        .staticMethod("parseInt", asList(StringValue.REF),
            (environment, arguments) -> {
                val value = (StringInterpreterValue)arguments.get(0);
                return new IntegerInterpreterValue(Integer.parseInt(value.getValue()));
            })
        
        .build();
    
    int value;

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
}
