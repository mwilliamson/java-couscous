package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.TypeName;

public class TypeValue implements PrimitiveValue {
    private final TypeName type;

    public TypeValue(TypeName type) {
        this.type = type;
    }

    @Override
    public <T> T accept(PrimitiveValueVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public TypeName getType() {
        return ObjectValues.CLASS;
    }

    @Override
    public String toString() {
        return "TypeValue(" +
            "type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeValue typeValue = (TypeValue) o;

        return type.equals(typeValue.type);

    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
