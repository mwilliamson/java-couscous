package org.zwobble.couscous.ast.identifiers;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.util.ExtraLists;

import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.list;

public class Identifier {
    public static final Identifier TOP = new Identifier(list());

    public static Identifier forType(String name) {
        return TOP.type(name);
    }

    public static Identifier forType(ScalarType type) {
        return forType(type.getQualifiedName());
    }

    private final List<IdentifierPart> parts;

    Identifier(List<IdentifierPart> parts) {
        this.parts = parts;
    }

    public Iterable<IdentifierPart> getParts() {
        return parts;
    }

    public Identifier type(String name) {
        return extend(name, IdentifierType.TYPE);
    }

    public Identifier method(String name) {
        return extend(name, IdentifierType.METHOD);
    }

    public Identifier constructor() {
        return extend("", IdentifierType.CONSTRUCTOR);
    }

    public Identifier variable(String name) {
        return extend(name, IdentifierType.VARIABLE);
    }

    private Identifier extend(String name, IdentifierType type) {
        return new Identifier(ExtraLists.append(parts, new IdentifierPart(name, type)));
    }

    @Override
    public String toString() {
        return "Identifier(" +
            "parts=" + parts +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        return parts.equals(that.parts);

    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }
}
