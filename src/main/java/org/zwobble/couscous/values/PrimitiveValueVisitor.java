package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.TypeName;

public interface PrimitiveValueVisitor<T> {
    T visitInteger(int value);
    T visitString(String value);
    T visitBoolean(boolean value);
    T visitUnit();
    T visitType(TypeName value);
}
