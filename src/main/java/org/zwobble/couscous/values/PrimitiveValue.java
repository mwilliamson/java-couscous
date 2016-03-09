package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.types.ScalarType;

public interface PrimitiveValue {
    <T> T accept(PrimitiveValueVisitor<T> visitor);
    ScalarType getType();
}
