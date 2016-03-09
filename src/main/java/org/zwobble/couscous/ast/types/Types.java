package org.zwobble.couscous.ast.types;

import org.zwobble.couscous.values.ObjectValues;

public class Types {
    public static ScalarType erasure(Type type) {
        return type.accept(new Type.Visitor<ScalarType>() {
            @Override
            public ScalarType visit(ScalarType type) {
                return type;
            }

            @Override
            public ScalarType visit(TypeParameter parameter) {
                return ObjectValues.OBJECT;
            }

            @Override
            public ScalarType visit(ParameterizedType type) {
                return type.getRawType();
            }

            @Override
            public ScalarType visit(BoundTypeParameter type) {
                return erasure(type.getParameter());
            }
        });
    }
}
