package org.zwobble.couscous.ast.types;

public class Types {
    public static ScalarType erasure(Type type) {
        return type.accept(new Type.Visitor<ScalarType>() {
            @Override
            public ScalarType visit(ScalarType type) {
                return type;
            }
        });
    }
}
