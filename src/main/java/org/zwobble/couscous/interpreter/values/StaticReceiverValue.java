package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.Executor;

import java.util.List;

public class StaticReceiverValue implements ReceiverValue {
    private final ConcreteType type;
    private final InterpreterFields fields;

    public StaticReceiverValue(ConcreteType type) {
        this.type = type;
        this.fields = new InterpreterFields(type);
    }

    @Override
    public InterpreterValue callMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments) {
        return type.callStaticMethod(environment, signature, arguments);
    }

    @Override
    public InterpreterValue getField(String fieldName) {
        return fields.getField(fieldName);
    }

    @Override
    public void setField(String fieldName, InterpreterValue value) {
        fields.setField(fieldName, value);
    }

    public void callStaticConstructor(Environment environment) {
        Executor.exec(environment, type.getStaticConstructor());
    }

    public InterpreterValue callConstructor(Environment environment, List<InterpreterValue> arguments) {
        return type.callConstructor(environment, arguments);
    }
}
