package org.zwobble.couscous.types;

public class MethodTypeParameter implements Type {
    public static MethodTypeParameter methodTypeParameter(String name) {
        return new MethodTypeParameter(name);
    }

    private final String name;

    public MethodTypeParameter(String name) {
        this.name = name;
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
        return "MethodTypeParameter(" +
            "name=" + name +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodTypeParameter that = (MethodTypeParameter) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
