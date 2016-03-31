package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.Environment;

public interface ReceiverValue {
    InterpreterValue callMethod(Environment environment, MethodSignature signature, Arguments arguments);
    InterpreterValue getField(String fieldName);
    void setField(String fieldName, InterpreterValue value);
}
