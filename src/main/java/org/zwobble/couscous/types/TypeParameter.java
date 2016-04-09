package org.zwobble.couscous.types;

import org.zwobble.couscous.ast.identifiers.Identifier;

public class TypeParameter implements Type {
    public static TypeParameter typeParameter(Identifier declaringScope, String name) {
        return new TypeParameter(declaringScope, name);
    }

    private final Identifier declaringScope;
    private final String name;

    public TypeParameter(Identifier declaringScope, String name) {
        this.declaringScope = declaringScope;
        this.name = name;
    }

    public Identifier getDeclaringScope() {
        return declaringScope;
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
            "declaringScope=" + declaringScope +
            ", name=" + name +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeParameter that = (TypeParameter) o;

        if (!declaringScope.equals(that.declaringScope)) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = declaringScope.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
