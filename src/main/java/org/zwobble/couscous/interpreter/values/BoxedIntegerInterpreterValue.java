package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.types.Types;

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

    public static final InterpreterType TYPE = IntrinsicInterpreterType.classBuilder("java.lang.Integer")
        .field("value", Types.INT)

        .staticMethod("parseInt", list(Types.STRING), Types.INT,
            (environment, arguments) -> {
                StringInterpreterValue value = (StringInterpreterValue)arguments.get(0);
                return new IntegerInterpreterValue(Integer.parseInt(value.getValue()));
            })

        .staticMethod("valueOf", list(Types.INT), Types.BOXED_INT,
            (environment, arguments) -> {
                IntegerInterpreterValue value = (IntegerInterpreterValue)arguments.get(0);
                return of(value);
            })

        .method("equals", list(Types.OBJECT), Types.BOOLEAN,
            (environment, arguments) -> {
                InterpreterValue right = arguments.getPositionalArguments().get(0);
                if (right.getType().getType().equals(Types.BOXED_INT)) {
                    IntegerInterpreterValue leftValue = (IntegerInterpreterValue)arguments.getReceiver().getField("value");
                    IntegerInterpreterValue rightValue = (IntegerInterpreterValue)right.getField("value");
                    return BooleanInterpreterValue.of(leftValue.getValue() == rightValue.getValue());
                } else {
                    return BooleanInterpreterValue.of(false);
                }
            })

        .method("toString", list(), Types.STRING,
            (environment, arguments) -> {
                IntegerInterpreterValue value = (IntegerInterpreterValue)arguments.getReceiver().getField("value");
                return StringInterpreterValue.of(Integer.toString(value.getValue()));
            })

        .build();
}
