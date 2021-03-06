package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.interpreter.errors.UnexpectedValueType;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.types.Types;

public class InterpreterTypes {
    public static void checkIsInstance(Type type, InterpreterValue value) {
        checkIsSubType(type, value.getType());
    }

    private static void checkIsSubType(Type superType, InterpreterType subType) {
        if (!isSubType(superType, subType)) {
            throw new UnexpectedValueType(superType, subType.getType());
        }
    }

    public static boolean isSubType(Type superType, InterpreterType subType) {
        return
            superType.equals(subType.getType()) ||
            superType.equals(Types.OBJECT) ||
            subType.getSuperTypes().contains(superType);
    }
}
