package org.zwobble.couscous.types;

import java.util.List;
import java.util.Map;

import static org.zwobble.couscous.types.ParameterizedType.parameterizedType;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class Types {
    public static final ScalarType VOID = ScalarType.topLevel("void");
    public static final ScalarType BOOLEAN = ScalarType.topLevel("boolean");
    public static final ScalarType INT = ScalarType.topLevel("int");
    public static final ScalarType CHAR = ScalarType.topLevel("char");
    public static final ScalarType STRING = ScalarType.topLevel("java.lang.String");
    public static final ScalarType CLASS = ScalarType.topLevel("java.lang.Class");
    public static final ScalarType OBJECT = ScalarType.topLevel("java.lang.Object");
    public static final ScalarType BOXED_INT = ScalarType.topLevel("java.lang.Integer");
    public static final ScalarType BOXED_BOOLEAN = ScalarType.topLevel("java.lang.Boolean");
    public static final ScalarType ARRAY = ScalarType.topLevel("array");

    public static Type addTypeParameters(ScalarType rawType, List<Type> typeParameters) {
        if (typeParameters.isEmpty()) {
            return rawType;
        } else {
            return parameterizedType(rawType, typeParameters);
        }
    }

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

    public static Type substitute(Type type, Map<TypeParameter, Type> replacements) {
        return type.accept(new Type.Visitor<Type>() {
            @Override
            public Type visit(ScalarType type) {
                return type;
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

    public static Type concrete(Type type) {
        return type.accept(new Type.Visitor<Type>() {
            @Override
            public Type visit(ScalarType type) {
                return type;
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
                return type.getValue();
            }
        });
    }
}
