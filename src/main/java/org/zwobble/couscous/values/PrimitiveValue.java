package org.zwobble.couscous.values;

import org.zwobble.couscous.ast.TypeName;

public interface PrimitiveValue {
    <T> T accept(PrimitiveValueVisitor<T> visitor);
    TypeName getType();
}
