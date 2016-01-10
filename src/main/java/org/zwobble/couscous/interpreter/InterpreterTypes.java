package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.values.ObjectValues;

public class InterpreterTypes {
    public static void checkIsInstance(TypeName type, InterpreterValue value) {
        checkIsSubType(type, value.getType());
    }

    private static void checkIsSubType(TypeName superTypeName, ConcreteType subType) {
        if (!isSubType(superTypeName, subType)) {
            throw new UnexpectedValueType(superTypeName, subType.getName());
        }
    }

    public static boolean isSubType(TypeName superTypeName, ConcreteType subType) {
        return
            superTypeName.equals(subType.getName()) ||
            superTypeName.equals(ObjectValues.OBJECT) ||
            subType.getSuperTypes().contains(superTypeName);
    }
}
