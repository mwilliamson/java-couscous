package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.interpreter.InterpreterTypes;
import org.zwobble.couscous.interpreter.errors.NoSuchField;
import org.zwobble.couscous.interpreter.errors.UnboundField;
import org.zwobble.couscous.interpreter.types.InterpreterType;

import java.util.HashMap;
import java.util.Map;

public class InterpreterFields {
    public static InterpreterFields forClass(InterpreterType type) {
        return new InterpreterFields(true, type);
    }

    public static InterpreterFields forInstanceOf(InterpreterType type) {
        return new InterpreterFields(false, type);
    }

    private final boolean isStatic;
    private final InterpreterType type;
    private final Map<String, InterpreterValue> fields;

    private InterpreterFields(boolean isStatic, InterpreterType type) {
        this.isStatic = isStatic;
        this.type = type;
        this.fields = new HashMap<>();
    }

    public InterpreterValue getField(String fieldName) {
        getFieldDefinition(fieldName);
        if (fields.containsKey(fieldName)) {
            return fields.get(fieldName);
        } else {
            throw new UnboundField(fieldName);
        }
    }

    public void setField(String fieldName, InterpreterValue value) {
        FieldDeclarationNode field = getFieldDefinition(fieldName);
        InterpreterTypes.checkIsInstance(field.getType(), value);
        fields.put(fieldName, value);
    }

    private FieldDeclarationNode getFieldDefinition(String fieldName) {
        return type.getField(fieldName)
            .filter(field -> field.isStatic() == isStatic)
            .orElseThrow(() -> new NoSuchField(fieldName));
    }
}
