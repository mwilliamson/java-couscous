package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.types.ScalarType;

public interface PrimitiveValue {
    <T> T accept(Visitor<T> visitor);
    ScalarType getType();

    interface Visitor<T> {
        T visitInteger(int value);
        T visitString(String value);
        T visitBoolean(boolean value);
        T visitUnit();
        T visitType(ScalarType value);
    }
}
