package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.backends.csharp.primitives.CsharpPrimitiveMethods;
import org.zwobble.couscous.backends.naming.Naming;
import org.zwobble.couscous.transforms.*;
import org.zwobble.couscous.types.*;

import java.util.List;
import java.util.Map;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class CsharpCodeGenerator {
    public static final Naming NAMING = Naming.noMangling();

    private final static Map<ScalarType, ScalarType> PRIMITIVES = ImmutableMap.<ScalarType, ScalarType>builder()
        .put(Types.INT, ScalarType.topLevel("int"))
        .put(Types.BOXED_INT, ScalarType.topLevel("int"))
        .put(Types.STRING, ScalarType.topLevel("string"))
        .put(Types.CHAR, ScalarType.topLevel("char"))
        .put(Types.BOOLEAN, ScalarType.topLevel("bool"))
        .put(Types.BOXED_BOOLEAN, ScalarType.topLevel("bool"))
        .put(Types.CLASS, ScalarType.topLevel("System.Type"))
        .put(Types.VOID, ScalarType.topLevel("void"))
        .put(Types.ARRAY, Types.ARRAY)
        .build();

    private final String namespace;
    private final NodeTransformer nodeTransformer;

    public static List<TypeNode> generateCode(List<TypeNode> types, String namespace) {
        return new CsharpCodeGenerator(NAMING, namespace).generateCode(types);
    }

    private CsharpCodeGenerator(Naming naming, String namespace) {
        this.namespace = namespace;
        nodeTransformer = NodeTransformer.builder()
            .transformType(this::transformType)
            .transformMethodName(naming::methodName)
            .transformFieldName(name -> "_" + name)
            .build();
    }

    private List<TypeNode> generateCode(List<TypeNode> types) {
        return SplitStaticsFromInterface.transform(
            NodeTransformer.applyAll(
                list(
                    DesugarForEachToFor.transformer(),
                    DesugarForToWhile.transformer(),
                    CsharpPrimitiveMethods.TRANSFORMER,
                    nodeTransformer
                ),
                HoistNestedTypes.hoist(eagerMap(types, AnonymousClassToInnerClass::transform))
            )
        );
    }

    private Type transformType(Type type) {
        return type.accept(new Type.Visitor<Type>() {
            @Override
            public Type visit(ScalarType type) {
                if (PRIMITIVES.containsKey(type)) {
                    return PRIMITIVES.get(type);
                } else {
                    return addPrefix(type, namespace);
                }
            }

            @Override
            public TypeParameter visit(TypeParameter parameter) {
                return parameter;
            }

            @Override
            public Type visit(ParameterizedType type) {
                if (type.getRawType().equals(Types.CLASS)) {
                    return ScalarType.topLevel("System.Type");
                } else {
                    return new ParameterizedType(
                        (ScalarType) transformType(type.getRawType()),
                        eagerMap(type.getParameters(), parameter -> transformType(parameter)));
                }
            }

            @Override
            public Type visit(BoundTypeParameter type) {
                return new BoundTypeParameter(
                    visit(type.getParameter()),
                    transformType(type.getValue()));
            }
        });
    }

    private Type addPrefix(Type type, String namespace) {
        return type.accept(new Type.Visitor<Type>() {
            @Override
            public ScalarType visit(ScalarType type) {
                return ScalarType.topLevel(namespace + "." + type.getQualifiedName());
            }

            @Override
            public TypeParameter visit(TypeParameter parameter) {
                return parameter;
            }

            @Override
            public Type visit(ParameterizedType type) {
                return new ParameterizedType(
                    visit(type.getRawType()),
                    eagerMap(type.getParameters(), parameter -> addPrefix(parameter, namespace)));
            }

            @Override
            public Type visit(BoundTypeParameter type) {
                return new BoundTypeParameter(
                    visit(type.getParameter()),
                    addPrefix(type.getValue(), namespace));
            }
        });
    }
}
