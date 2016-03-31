package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.Executor;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.ParameterizedInterpreterType;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Optional;

public class StaticReceiverValue implements ReceiverValue {
    private final InterpreterType type;
    private final InterpreterFields fields;

    public StaticReceiverValue(InterpreterType type) {
        this.type = type;
        this.fields = InterpreterFields.forClass(type);
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

    public InterpreterValue callConstructor(Environment environment, Optional<List<Type>> typeParameters, List<InterpreterValue> arguments) {
        InterpreterType objectType = typeParameters
            .<InterpreterType>map(parameters -> new ParameterizedInterpreterType(type, parameters))
            .orElse(type);
        ObjectInterpreterValue value = new ObjectInterpreterValue(objectType);
        type.callConstructor(environment, value, arguments);
        return value;
    }
}
