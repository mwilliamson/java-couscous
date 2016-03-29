package org.zwobble.couscous.types;

import static org.zwobble.couscous.util.ExtraLists.list;

public class Types {
    public static final ScalarType VOID = ScalarType.of("void");
    public static final ScalarType BOOLEAN = ScalarType.of("boolean");
    public static final ScalarType INT = ScalarType.of("int");
    public static final ScalarType STRING = ScalarType.of("java.lang.String");
    public static final ScalarType CLASS = ScalarType.of("java.lang.Class");
    public static final ScalarType OBJECT = ScalarType.of("java.lang.Object");
    public static final ScalarType BOXED_INT = ScalarType.of("java.lang.Integer");
    public static final ScalarType BOXED_BOOLEAN = ScalarType.of("java.lang.Boolean");
    public static final ScalarType ARRAY = ScalarType.of("array");

    public static Type array(Type elementType) {
        return new ParameterizedType(ARRAY, list(elementType));
    }

    public static ScalarType erasure(Type type) {
        return type.accept(new Type.Visitor<ScalarType>() {
            @Override
            public ScalarType visit(ScalarType type) {
                return type;
            }

            @Override
            public ScalarType visit(TypeParameter parameter) {
                return OBJECT;
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
