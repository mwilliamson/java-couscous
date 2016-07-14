package org.zwobble.couscous.ast.visitors;

import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import org.zwobble.couscous.ast.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.zwobble.couscous.util.ExtraMaps.entry;

public class DynamicNodeMapper {
    private static final Map<Map.Entry<Class<?>, String>, Function<?, Function>> VISITOR_BUILDERS
        = new HashMap<>();

    // TODO: check return type -- at the very least, check the methods are consistent.
    public static <T, R> Function<Node, R> instantiate(T visitor, String methodName) {
        Map.Entry<Class<?>, String> key = entry(visitor.getClass(), methodName);
        Function builder = VISITOR_BUILDERS.get(key);
        if (builder == null) {
            builder = DynamicNodeVisitor.buildClassSupplier(visitor.getClass(), Function.class, methodName, MethodReturn.REFERENCE);
            VISITOR_BUILDERS.put(key, builder);
        }
        return (Function<Node, R>) builder.apply(visitor);
    }
}
