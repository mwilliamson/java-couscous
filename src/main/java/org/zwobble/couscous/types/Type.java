package org.zwobble.couscous.types;

public interface Type {
    <T> T accept(Visitor<T> visitor);

    interface Visitor<T> {
        T visit(ScalarType type);
        T visit(TypeParameter parameter);
        T visit(ParameterizedType type);
        T visit(BoundTypeParameter type);
    }
}
