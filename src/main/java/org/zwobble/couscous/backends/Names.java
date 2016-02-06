package org.zwobble.couscous.backends;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.TypeName;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.couscous.util.ExtraLists.list;

public class Names {
    public static String toUniqueName(MethodSignature signature) {
        return Joiner.on("__").join(Iterables.concat(
            list(signature.getName()),
            transform(signature.getArguments(), Names::typeToString),
            list(typeToString(signature.getReturnType()))));
    }

    private static String typeToString(TypeName argument) {
        return argument.getQualifiedName().replace('.', '_');
    }
}
