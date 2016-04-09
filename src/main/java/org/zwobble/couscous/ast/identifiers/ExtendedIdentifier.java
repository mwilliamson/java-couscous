package org.zwobble.couscous.ast.identifiers;

import static org.zwobble.couscous.util.ExtraIterables.lazyAppend;

public class ExtendedIdentifier implements Identifier {
    private final Identifier parent;
    private final String part;
    private final IdentifierType type;

    ExtendedIdentifier(Identifier parent, String part, IdentifierType type) {
        this.parent = parent;
        this.part = part;
        this.type = type;
    }

    @Override
    public Iterable<IdentifierPart> getParts() {
        return lazyAppend(parent.getParts(), new IdentifierPart(part, type));
    }

    @Override
    public String toString() {
        return "ExtendedIdentifier(" +
            "parent=" + parent +
            ", part='" + part + '\'' +
            ", type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtendedIdentifier that = (ExtendedIdentifier) o;

        if (!parent.equals(that.parent)) return false;
        if (!part.equals(that.part)) return false;
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + part.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
