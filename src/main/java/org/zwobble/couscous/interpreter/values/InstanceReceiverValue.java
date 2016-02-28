package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.types.InterpreterType;

import java.util.List;

public class InstanceReceiverValue implements ReceiverValue {
    private final InterpreterValue value;

    public InstanceReceiverValue(InterpreterValue value) {
        this.value = value;
    }

    @Override
    public InterpreterValue callMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments) {
        InterpreterType type = value.getType();
        return type.callMethod(environment, value, signature, arguments);
    }

    @Override
    public InterpreterValue getField(String fieldName) {
        return value.getField(fieldName);
    }

    @Override
    public void setField(String fieldName, InterpreterValue value) {
        this.value.setField(fieldName, value);
    }
}
