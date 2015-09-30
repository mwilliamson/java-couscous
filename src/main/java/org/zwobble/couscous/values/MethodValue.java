package org.zwobble.couscous.values;

import java.util.List;

public interface MethodValue {
    InterpreterValue call(List<InterpreterValue> arguments);
}
