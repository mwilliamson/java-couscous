package org.zwobble.couscous.ast.visitors;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.util.ExtraLists.eagerFilter;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

/**
 * Dynamically generate a method to dispatch methods based on node type.
 *
 * <p>For instance, let's say you've defined two methods in a class:</p>
 *
 * <blockquote><pre>
 *     void visit(LiteralNode);
 *     void visit(VariableReferenceNode);
 * </pre></blockquote>
 *
 * Calling:
 *
 * <blockquote><pre>
 *     DynamicNodeVisitor.build(NodeWriter.class, "visit").instantiate(nodeWriter)
 * </pre></blockquote>
 *
 * will generate a function accepting a node equivalent to:
 *
 * <blockquote><pre>
 *     switch (node.nodeType()) {
 *         case NodeTypes.LITERAL:
 *             nodeWriter.visit((LiteralNode) node);
 *             return;
 *         case NodeTypes.VARIABLE_REFERENCE:
 *             nodeWriter.visit((VariableReferenceNode) node);
 *             return;
 *         default;
 *             return;
 *     }
 * </pre></blockquote>
 *
 * <p>
 *   Note that {@code build} is comparatively expensive,
 *   so should normally be called once separately from {@code instantiate}.
 * </p>
 *
 * <p>
 *   In answer to "why not define a visitor interface instead?":
 *   this allows visitors to be defined over a subset of nodes,
 *   rather than requiring a visit method to be defined for every node.
 *   Further, it allows extension of the Node interface,
 *   making it easier to mix and match transformations that may add or remove particular node types
 *   (which affects which node types later stages are required to handle).
 * </p>
 */
public class DynamicNodeVisitor<T> {
    public static <T> DynamicNodeVisitor<T> build(Class<T> clazz, String methodName) {
        Function<T, Consumer> builder = buildClassSupplier(clazz, Consumer.class, methodName, MethodReturn.VOID);
        return new DynamicNodeVisitor<>(builder);
    }

    public static <T, F> Function<T, F> buildClassSupplier(Class<T> clazz, Class<F> function, String methodName, MethodReturn methodReturn) {
        Class<? extends F> visitorClass = buildClass(clazz, function, methodName, methodReturn);

        try {
            return new ByteBuddy()
                .subclass(Function.class)

                .method(ElementMatchers.isAbstract())
                .intercept(Implementations.stackManipulation(target -> new StackManipulation.Compound(
                    TypeCreation.of(new TypeDescription.ForLoadedType(visitorClass)),
                    Duplication.SINGLE,
                    MethodVariableAccess.REFERENCE.loadOffset(1),
                    TypeCasting.to(new TypeDescription.ForLoadedType(clazz)),
                    MethodInvocation.invoke(TypeDescriptions.findConstructor(visitorClass, clazz)),
                    MethodReturn.REFERENCE
                )))

                .make()
                .load(DynamicNodeVisitor.class.getClassLoader())
                .getLoaded()
                .newInstance();
        } catch (InstantiationException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static <T, F> Class<? extends F> buildClass(Class<T> clazz, Class<F> function, String methodName, MethodReturn methodReturn) {
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

        return new ByteBuddy()
            .subclass(function)

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

            .method(ElementMatchers.isAbstract())
            .intercept(Implementations.stackManipulation(target -> {
                MethodDescription typeMethod = TypeDescriptions.findMethod(Node.class, "type");
                return new StackManipulation.Compound(
                    MethodVariableAccess.REFERENCE.loadOffset(1),
                    MethodInvocation.invoke(typeMethod),
                    new StackManipulationSwitch(
                        defaultMethod
                            .map(defaultMethodValue ->
                                delegateToMethod(target, defaultMethodValue, Node.class, methodReturn))
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
                                delegateToMethod(target, method, nodeClass, methodReturn)
                            );
                        })
                    )
                );
            }))
            .make()
            .load(DynamicNodeVisitor.class.getClassLoader())
            .getLoaded();
    }

    private static StackManipulation.Compound delegateToMethod(Implementation.Target target, Method method, Class<?> nodeClass, MethodReturn methodReturn) {
        return new StackManipulation.Compound(
            MethodVariableAccess.REFERENCE.loadOffset(0),
            FieldAccess.forField(TypeDescriptions.findField(target, "visitor")).getter(),
            MethodVariableAccess.REFERENCE.loadOffset(1),
            TypeCasting.to(new TypeDescription.ForLoadedType(nodeClass)),
            MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(method)),
            methodReturn
        );
    }

    private static boolean isVisitMethod(String methodName, Method method) {
        return
            method.getName().equals(methodName) &&
            method.getParameterCount() == 1 &&
            Node.class.isAssignableFrom(method.getParameterTypes()[0]) &&
            !method.getParameterTypes()[0].equals(Node.class);
    }

    private final Function<T, Consumer> visitor;

    private DynamicNodeVisitor(Function<T, Consumer> visitor) {
        this.visitor = visitor;
    }

    public Consumer<Node> instantiate(T instance) {
        return visitor.apply(instance);
    }
}
