package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.InterpreterTypes;
import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.interpreter.errors.UnboundField;

import java.util.HashMap;
import java.util.Map;

public class InterpreterFields {
    // TODO: distinguish static and non-static fields

    private final ConcreteType type;
    private final Map<String, InterpreterValue> fields;

    public InterpreterFields(ConcreteType type) {
        this.type = type;
        this.fields = new HashMap<>();
    }

    public InterpreterValue getField(String fieldName) {
        type.getField(fieldName).orElseThrow(() -> new NoSuchField(fieldName));
        if (fields.containsKey(fieldName)) {
            return fields.get(fieldName);
        } else {
            throw new UnboundField(fieldName);
        }
    }

    public void setField(String fieldName, InterpreterValue value) {
        FieldValue field = type.getField(fieldName)
            .orElseThrow(() -> new NoSuchField(fieldName));
        InterpreterTypes.checkIsInstance(field.getType(), value);
        fields.put(fieldName, value);
    }
}
