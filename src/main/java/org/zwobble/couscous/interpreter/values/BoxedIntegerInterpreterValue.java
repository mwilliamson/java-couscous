package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.Operator;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;

public class BoxedIntegerInterpreterValue {
    public static final ConcreteType TYPE = ConcreteType.classBuilder("java.lang.Integer")
        .field("value", IntegerValue.REF)

        .staticMethod("parseInt", asList(StringValue.REF),
            (environment, arguments) -> {
                StringInterpreterValue value = (StringInterpreterValue)arguments.get(0);
                return new IntegerInterpreterValue(Integer.parseInt(value.getValue()));
            })

        .method(Operator.EQUALS.getMethodName(), asList(ObjectValues.OBJECT),
            (environment, arguments) -> {
                InterpreterValue right = arguments.getPositionalArguments().get(0);
                if (right.getType().getName().equals(ObjectValues.BOXED_INT)) {
                    IntegerInterpreterValue leftValue = (IntegerInterpreterValue)arguments.getReceiver().getField("value");
                    IntegerInterpreterValue rightValue = (IntegerInterpreterValue)right.getField("value");
                    return new BooleanInterpreterValue(leftValue.getValue() == rightValue.getValue());
                } else {
                    return new BooleanInterpreterValue(false);
                }
            })
        .build();
}
