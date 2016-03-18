package org.zwobble.couscous.types;

public class BoundTypeParameter implements Type {
    public static BoundTypeParameter boundTypeParameter(TypeParameter parameter, Type value) {
        return new BoundTypeParameter(parameter, value);
    }

    private final TypeParameter parameter;
    private final Type value;

    public BoundTypeParameter(TypeParameter parameter, Type value) {
        this.parameter = parameter;
        this.value = value;
    }

    public TypeParameter getParameter() {
        return parameter;
    }

    public Type getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "BoundTypeParameter(" +
            "parameter=" + parameter +
            ", value=" + value +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoundTypeParameter that = (BoundTypeParameter) o;

        if (!parameter.equals(that.parameter)) return false;
        return value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = parameter.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
