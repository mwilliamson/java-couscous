package org.zwobble.couscous.values;

public interface PrimitiveValue {
    <T> T accept(PrimitiveValueVisitor<T> visitor);
}
