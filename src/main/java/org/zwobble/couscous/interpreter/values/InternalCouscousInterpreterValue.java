package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.types.Types;

import static org.zwobble.couscous.util.ExtraLists.list;

public class InternalCouscousInterpreterValue {
    public static final InterpreterType TYPE = IntrinsicInterpreterType.classBuilder("_couscous")
        .staticMethod("same", list(Types.OBJECT, Types.OBJECT), Types.BOOLEAN,
            (environment, arguments) ->
                BooleanInterpreterValue.of(arguments.get(0) == arguments.get(1)))

        .build();
}
