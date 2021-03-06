package org.zwobble.couscous.backends.naming;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.types.*;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.couscous.util.ExtraIterables.lazyMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class Names {
    public static String toUniqueName(MethodSignature signature) {
        return Joiner.on("__").join(Iterables.concat(
            list(signature.getName()),
            transform(signature.getArguments(), Names::typeToString),
            list(typeToString(signature.getReturnType()))));
    }

    private static String typeToString(Type argument) {
        return argument.accept(new Type.Visitor<String>() {
            @Override
            public String visit(ScalarType type) {
                return type.getSimpleName();
            }

            @Override
            public String visit(TypeParameter parameter) {
                // TODO: should we care about the type?
                Iterable<String> parts = lazyMap(parameter.getDeclaringScope().getParts(), part -> part.getName().replace(".", "_"));
                return String.join("_", parts) + "_" + parameter.getName();
            }

            @Override
            public String visit(ParameterizedType type) {
                return typeToString(type.getRawType());
            }

            @Override
            public String visit(BoundTypeParameter type) {
                return typeToString(type.getParameter());
            }
        });
    }
}
