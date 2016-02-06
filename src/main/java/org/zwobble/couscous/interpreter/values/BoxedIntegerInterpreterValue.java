package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;
import org.zwobble.couscous.values.StringValue;

import static org.zwobble.couscous.util.ExtraLists.list;

public class BoxedIntegerInterpreterValue {
    public static ObjectInterpreterValue of(int value) {
        return of(new IntegerInterpreterValue(value));
    }

    public static ObjectInterpreterValue of(IntegerInterpreterValue value) {
        ObjectInterpreterValue obj = new ObjectInterpreterValue(TYPE);
        obj.setField("value", value);
        return obj;
    }

    public static final ConcreteType TYPE = ConcreteType.classBuilder("java.lang.Integer")
        .field("value", IntegerValue.REF)

        .staticMethod("parseInt", list(StringValue.REF), IntegerValue.REF,
            (environment, arguments) -> {
                StringInterpreterValue value = (StringInterpreterValue)arguments.get(0);
                return new IntegerInterpreterValue(Integer.parseInt(value.getValue()));
            })

        .staticMethod("valueOf", list(IntegerValue.REF), ObjectValues.BOXED_INT,
            (environment, arguments) -> {
                IntegerInterpreterValue value = (IntegerInterpreterValue)arguments.get(0);
                return of(value);
            })

        .method("equals", list(ObjectValues.OBJECT), BooleanValue.REF,
            (environment, arguments) -> {
                InterpreterValue right = arguments.getPositionalArguments().get(0);
                if (right.getType().getName().equals(ObjectValues.BOXED_INT)) {
                    IntegerInterpreterValue leftValue = (IntegerInterpreterValue)arguments.getReceiver().getField("value");
                    IntegerInterpreterValue rightValue = (IntegerInterpreterValue)right.getField("value");
                    return BooleanInterpreterValue.of(leftValue.getValue() == rightValue.getValue());
                } else {
                    return BooleanInterpreterValue.of(false);
                }
            })

        .method("toString", list(), StringValue.REF,
            (environment, arguments) -> {
                IntegerInterpreterValue value = (IntegerInterpreterValue)arguments.getReceiver().getField("value");
                return StringInterpreterValue.of(Integer.toString(value.getValue()));
            })

        .build();
}
