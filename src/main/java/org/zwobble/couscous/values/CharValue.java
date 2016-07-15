package org.zwobble.couscous.values;

import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

public class CharValue implements PrimitiveValue {
    private final char value;

    public CharValue(char value) {
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitChar(value);
    }

    @Override
    public Type getType() {
        return Types.CHAR;
    }

    @Override
    public String toString() {
        return "CharValue(" +
            "value=" + value +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CharValue charValue = (CharValue) o;

        return value == charValue.value;

    }

    @Override
    public int hashCode() {
        return (int) value;
    }
}
