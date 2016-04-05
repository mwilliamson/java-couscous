package org.zwobble.couscous.types;

import java.util.Map;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;
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
            public ScalarType visit(MethodTypeParameter parameter) {
                return OBJECT;
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

    public static Type substitute(Type type, Map<TypeParameter, Type> replacements) {
        return type.accept(new Type.Visitor<Type>() {
            @Override
            public Type visit(ScalarType type) {
                return type;
            }

            @Override
            public Type visit(MethodTypeParameter parameter) {
                return parameter;
            }

            @Override
            public Type visit(TypeParameter type) {
                return replacements.getOrDefault(type, type);
            }

            @Override
            public Type visit(ParameterizedType type) {
                return new ParameterizedType(
                    type.getRawType(),
                    eagerMap(type.getParameters(), parameter -> parameter.accept(this)));
            }

            @Override
            public Type visit(BoundTypeParameter type) {
                throw new UnsupportedOperationException();
            }
        });
    }

    public static Type generic(Type type) {
        return type.accept(new Type.Visitor<Type>() {
            @Override
            public Type visit(ScalarType type) {
                return type;
            }

            @Override
            public Type visit(MethodTypeParameter parameter) {
                return parameter;
            }

            @Override
            public Type visit(TypeParameter parameter) {
                return parameter;
            }

            @Override
            public Type visit(ParameterizedType type) {
                return new ParameterizedType(
                    type.getRawType(),
                    eagerMap(type.getParameters(), parameter -> parameter.accept(this)));
            }

            @Override
            public Type visit(BoundTypeParameter type) {
                return type.getParameter();
            }
        });
    }
}
