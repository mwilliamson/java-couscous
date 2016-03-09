package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.types.ScalarType;

public class TypeValue implements PrimitiveValue {
    private final ScalarType value;

    public TypeValue(ScalarType value) {
        this.value = value;
    }

    public ScalarType getValue() {
        return value;
    }

    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visitType(value);
    }

    @Override
    public ScalarType getType() {
        return ObjectValues.CLASS;
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
