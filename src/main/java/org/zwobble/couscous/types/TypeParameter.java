package org.zwobble.couscous.types;

public class TypeParameter implements Type {
    public static TypeParameter typeParameter(ScalarType declaringType, String name) {
        return new TypeParameter(declaringType, name);
    }

    private final ScalarType declaringType;
    private final String name;

    public TypeParameter(ScalarType declaringType, String name) {
        this.declaringType = declaringType;
        this.name = name;
    }

    public ScalarType getDeclaringType() {
        return declaringType;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "TypeParameter(" +
            "declaringType=" + declaringType +
            ", name=" + name +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeParameter that = (TypeParameter) o;

        if (!declaringType.equals(that.declaringType)) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = declaringType.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
