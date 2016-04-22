package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.types.Type;

public class AnonymousType implements Type {
    public static Type anonymousType(String key) {
        return new AnonymousType(key);
    }

    private final String key;

    public AnonymousType(String key) {
        this.key = key;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnonymousType that = (AnonymousType) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
