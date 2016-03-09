package org.zwobble.couscous.ast.types;

public interface Type {
    <T> T accept(Visitor<T> visitor);

    interface Visitor<T> {
        T visit(ScalarType type);
    }
}
