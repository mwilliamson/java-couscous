package org.zwobble.couscous.ast.visitors;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.Throw;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.matcher.ElementMatchers;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.NodeTypes;
import org.zwobble.couscous.util.ExtraIterables;
import org.zwobble.couscous.util.asm.Implementations;
import org.zwobble.couscous.util.asm.StackManipulationSwitch;
import org.zwobble.couscous.util.asm.TypeDescriptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.util.ExtraLists.eagerFilter;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class DynamicNodeMapper<T, R> {
    public static <T, R> DynamicNodeMapper<T, R> build(Class<T> clazz, Class<R> returnType, String methodName) {
        return build(clazz, new TypeDescription.ForLoadedType(returnType), methodName);
    }

    public static <T, R> DynamicNodeMapper<T, R> build(Class<T> clazz, TypeDescription returnType, String methodName) {
        List<Method> visitMethods = eagerFilter(
            asList(clazz.getMethods()),
            method -> isVisitMethod(methodName, method)
        );

        Optional<Method> defaultMethod = ExtraIterables.find(
            asList(clazz.getMethods()),
            method ->
                method.getName().equals(methodName) &&
                method.getParameterCount() == 1 &&
                method.getParameterTypes()[0].equals(Node.class)
        );

        Class<? extends Function> visitor = new ByteBuddy()
            .subclass(Function.class)

            .defineField("visitor", clazz, Visibility.PRIVATE, FieldManifestation.FINAL)

            .defineConstructor(Visibility.PUBLIC)
            .withParameters(clazz)
            .intercept(Implementations.stackManipulation(target -> new StackManipulation.Compound(
                // this()
                MethodVariableAccess.REFERENCE.loadOffset(0),
                MethodInvocation.invoke(TypeDescriptions.findConstructor(Object.class)),

                // this.visitor = visitor
                MethodVariableAccess.REFERENCE.loadOffset(0),
                MethodVariableAccess.REFERENCE.loadOffset(1),
                FieldAccess.forField(TypeDescriptions.findField(target, "visitor")).putter(),

                // return
                MethodReturn.VOID
            )))

            .method(ElementMatchers.named("apply"))
            .intercept(Implementations.stackManipulation(target -> {
                MethodDescription typeMethod = TypeDescriptions.findMethod(Node.class, "type");
                return new StackManipulation.Compound(
                    MethodVariableAccess.REFERENCE.loadOffset(1),
                    MethodInvocation.invoke(typeMethod),
                    new StackManipulationSwitch(
                        defaultMethod
                            .map(defaultMethodValue ->
                                new StackManipulation.Compound(
                                    MethodVariableAccess.REFERENCE.loadOffset(0),
                                    FieldAccess.forField(TypeDescriptions.findField(target, "visitor")).getter(),
                                    MethodVariableAccess.REFERENCE.loadOffset(1),
                                    TypeCasting.to(new TypeDescription.ForLoadedType(Node.class)),
                                    MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(defaultMethodValue)),
                                    MethodReturn.REFERENCE
                                ))
                            .orElseGet(() -> new StackManipulation.Compound(
                                TypeCreation.of(new TypeDescription.ForLoadedType(UnsupportedOperationException.class)),
                                Duplication.SINGLE,
                                MethodInvocation.invoke(TypeDescriptions.findConstructor(UnsupportedOperationException.class)),
                                Throw.INSTANCE
                            )),
                        eagerMap(visitMethods, method -> {
                            Class<?> nodeClass = method.getParameterTypes()[0];
                            return StackManipulationSwitch.switchCase(
                                NodeTypes.forClass(nodeClass),
                                new StackManipulation.Compound(
                                    MethodVariableAccess.REFERENCE.loadOffset(0),
                                    FieldAccess.forField(TypeDescriptions.findField(target, "visitor")).getter(),
                                    MethodVariableAccess.REFERENCE.loadOffset(1),
                                    TypeCasting.to(new TypeDescription.ForLoadedType(nodeClass)),
                                    MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(method)),
                                    MethodReturn.REFERENCE
                                )
                            );
                        })
                    )
                );
            }))
            .make()
            .load(DynamicNodeMapper.class.getClassLoader())
            .getLoaded();

        return new DynamicNodeMapper<T, R>(clazz, visitor);
    }

    private static boolean isVisitMethod(String methodName, Method method) {
        return
            method.getName().equals(methodName) &&
            method.getParameterCount() == 1 &&
            Node.class.isAssignableFrom(method.getParameterTypes()[0]) &&
            !method.getParameterTypes()[0].equals(Node.class);
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
