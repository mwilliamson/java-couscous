package org.zwobble.couscous.ast.visitors;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import org.zwobble.couscous.ast.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class DynamicNodeMapper<T, R> {
    public static <T, R> DynamicNodeMapper<T, R> build(Class<T> clazz, Class<R> returnType, String methodName) {
        return build(clazz, new TypeDescription.ForLoadedType(returnType), methodName);
    }

    public static <T, R> DynamicNodeMapper<T, R> build(Class<T> clazz, TypeDescription returnType, String methodName) {
        return new DynamicNodeMapper<>(
            clazz,
            DynamicNodeVisitor.buildClass(clazz, Function.class, methodName, MethodReturn.REFERENCE)
        );
    }

    private final Class<T> clazz;
    private final Class<? extends Function> visitor;

    public DynamicNodeMapper(Class<T> clazz, Class<? extends Function> visitor) {
        this.clazz = clazz;
        this.visitor = visitor;
    }

    public Function<Node, R> instantiate(T instance) {
        try {
            return visitor.getConstructor(clazz).newInstance(instance);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }
}
