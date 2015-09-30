package org.zwobble.couscous.values;

import java.util.Optional;

public interface InterpreterValue {
    Optional<MethodValue> getMethod(String methodName);
}
