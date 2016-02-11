package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.interpreter.Environment;

import java.util.List;

public class StaticReceiverValue implements ReceiverValue {
    private final ConcreteType type;

    public StaticReceiverValue(ConcreteType type) {
        this.type = type;
    }

    @Override
    public InterpreterValue callMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments) {
        return type.callStaticMethod(environment, signature, arguments);
    }
}
