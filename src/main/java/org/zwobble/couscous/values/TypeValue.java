package org.zwobble.couscous.values;

import org.zwobble.couscous.types.ParameterizedType;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;

import static org.zwobble.couscous.util.ExtraLists.list;

public class TypeValue implements PrimitiveValue {
    private final ScalarType value;

    public TypeValue(ScalarType value) {
        this.value = value;
    }

    public ScalarType getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitType(value);
    }

    @Override
    public Type getType() {
        return new ParameterizedType(ObjectValues.CLASS, list(value));
    }

    @Override
    public String toString() {
        return "TypeValue(" +
            "value=" + value +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeValue typeValue = (TypeValue) o;

        return value.equals(typeValue.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
