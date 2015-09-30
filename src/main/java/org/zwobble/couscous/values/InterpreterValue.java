package org.zwobble.couscous.values;

import java.util.List;

public interface InterpreterValue {
    InterpreterValue callMethod(String methodName, List<InterpreterValue> arguments);
}
