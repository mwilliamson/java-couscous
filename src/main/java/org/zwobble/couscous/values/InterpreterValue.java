package org.zwobble.couscous.values;

public interface InterpreterValue {
    InterpreterValue callMethod(String methodName);
}
