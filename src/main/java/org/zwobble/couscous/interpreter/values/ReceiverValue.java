package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.interpreter.Environment;

import java.util.List;

public interface ReceiverValue {
    InterpreterValue callMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments);
}
