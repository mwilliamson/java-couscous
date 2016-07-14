package org.zwobble.couscous.ast.visitors;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import org.zwobble.couscous.ast.Node;

import java.util.function.Function;

public class DynamicNodeMapper<T, R> {
    public static <T, R> DynamicNodeMapper<T, R> build(Class<T> clazz, Class<R> returnType, String methodName) {
        return build(clazz, new TypeDescription.ForLoadedType(returnType), methodName);
    }

    public static <T, R> DynamicNodeMapper<T, R> build(Class<T> clazz, TypeDescription returnType, String methodName) {
        return new DynamicNodeMapper<>(
            DynamicNodeVisitor.buildClassSupplier(clazz, Function.class, methodName, MethodReturn.REFERENCE)
        );
    }

    private final Function<T, Function> visitor;

    public DynamicNodeMapper(Function<T, Function> visitor) {
        this.visitor = visitor;
    }

    public Function<Node, R> instantiate(T instance) {
        return visitor.apply(instance);
    }
}
