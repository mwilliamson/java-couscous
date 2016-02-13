package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.TypeName;

public final class FieldValue {
    private final boolean isStatic;
    private final String name;
    private final TypeName type;

    public FieldValue(final boolean isStatic, final String name, final TypeName type) {
        this.isStatic = isStatic;
        this.name = name;
        this.type = type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getName() {
        return this.name;
    }

    public TypeName getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "FieldValue(" +
            "isStatic=" + isStatic +
            ", name='" + name + '\'' +
            ", type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldValue that = (FieldValue) o;

        if (isStatic != that.isStatic) return false;
        if (!name.equals(that.name)) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = (isStatic ? 1 : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}