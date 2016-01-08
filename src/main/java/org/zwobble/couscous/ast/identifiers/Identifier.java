package org.zwobble.couscous.ast.identifiers;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.list;

public class Identifier {
    public static final Identifier TOP = new Identifier(list());

    private final List<String> parts;

    private Identifier(List<String> parts) {
        this.parts = parts;
    }

    public Identifier extend(String name) {
        ImmutableList.Builder<String> parts = ImmutableList.builder();
        parts.addAll(this.parts);
        parts.add(name);
        return new Identifier(parts.build());
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
