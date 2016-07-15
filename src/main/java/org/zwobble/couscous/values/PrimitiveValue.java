package org.zwobble.couscous.values;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;

public interface PrimitiveValue {
    <T> T accept(Visitor<T> visitor);
    Type getType();

    interface Visitor<T> {
        T visitInteger(int value);
        T visitChar(char value);
        T visitString(String value);
        T visitBoolean(boolean value);
        T visitUnit();
        T visitType(ScalarType value);
    }
}
