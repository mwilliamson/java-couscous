package org.zwobble.couscous.ast.identifiers;

public class IdentifierPart {
    private final String name;
    private final IdentifierType type;

    public IdentifierPart(String name, IdentifierType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public IdentifierType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "IdentifierPart(" +
            "name='" + name + '\'' +
            ", type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdentifierPart that = (IdentifierPart) o;

        if (!name.equals(that.name)) return false;
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
