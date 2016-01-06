package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;

import static java.util.Arrays.asList;

public class InternalCouscousInterpreterValue {
    public static final ConcreteType TYPE = ConcreteType.classBuilder("_couscous")
        .staticMethod("same", asList(ObjectValues.OBJECT, ObjectValues.OBJECT),
            (environment, arguments) ->
                new BooleanInterpreterValue(arguments.get(0) == arguments.get(1)))

        .build();
}
