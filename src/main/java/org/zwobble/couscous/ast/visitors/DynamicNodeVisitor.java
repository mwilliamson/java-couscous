package org.zwobble.couscous.ast.visitors;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.NodeTypes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static org.zwobble.couscous.util.ExtraLists.eagerFilter;

public class DynamicNodeVisitor<T> {
    public static <T> DynamicNodeVisitor<T> build(Class<T> clazz, String methodName) {
        List<Method> visitMethods = eagerFilter(
            asList(clazz.getMethods()),
            method -> isVisitMethod(methodName, method)
        );

        Class<? extends Consumer> visitor = new ByteBuddy()
            .subclass(Consumer.class)

            .defineField("visitor", clazz, Visibility.PRIVATE, FieldManifestation.FINAL)

            .defineConstructor(Visibility.PUBLIC)
            .withParameters(clazz)
            .intercept(new Implementation() {
                @Override
                public InstrumentedType prepare(InstrumentedType instrumentedType) {
                    return instrumentedType;
                }

                @Override
                public ByteCodeAppender appender(Target implementationTarget) {
                    return new ByteCodeAppender() {
                        @Override
                        public Size apply(MethodVisitor methodVisitor, Context implementationContext, MethodDescription instrumentedMethod) {
                            StackManipulation.Size size = new StackManipulation.Compound(
                                // this()
                                MethodVariableAccess.REFERENCE.loadOffset(0),
                                MethodInvocation.invoke(new TypeDescription.ForLoadedType(Object.class)
                                    .getDeclaredMethods()
                                    .filter(isConstructor().and(takesArguments(0))).getOnly()),

                                // this.visitor = visitor
                                MethodVariableAccess.REFERENCE.loadOffset(0),
                                MethodVariableAccess.REFERENCE.loadOffset(1),
                                FieldAccess.forField(implementationTarget.getInstrumentedType()
                                    .getDeclaredFields()
                                    .filter(ElementMatchers.named("visitor"))
                                    .getOnly()).putter(),

                                // return
                                MethodReturn.VOID
                            ).apply(methodVisitor, implementationContext);
                            return new Size(size.getMaximalSize(), instrumentedMethod.getStackSize());
                        }
                    };
                }
            })

            .method(ElementMatchers.named("accept"))
            .intercept(new Implementation() {
                @Override
                public InstrumentedType prepare(InstrumentedType instrumentedType) {
                    return instrumentedType;
                }

                @Override
                public ByteCodeAppender appender(Target target) {
                    return new ByteCodeAppender() {
                        @Override
                        public Size apply(MethodVisitor methodVisitor, Context context, MethodDescription instrumentedMethod) {
                            Label defaultLabel = new Label();

                            MethodDescription typeMethod = new TypeDescription.ForLoadedType(Node.class)
                                .getDeclaredMethods()
                                .filter(ElementMatchers.named("type").and(ElementMatchers.takesArguments(0)))
                                .getOnly();
                            StackManipulation.Size size = new StackManipulation.Compound(
                                // Switch???
                                MethodVariableAccess.REFERENCE.loadOffset(1),
                                MethodInvocation.invoke(typeMethod),
                                new StackManipulation() {
                                    @Override
                                    public boolean isValid() {
                                        return true;
                                    }

                                    @Override
                                    public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
                                        Label literalLabel = new Label();
                                        methodVisitor.visitLookupSwitchInsn(
                                            defaultLabel,
                                            new int[]{NodeTypes.LITERAL},
                                            new Label[]{literalLabel}
                                        );

                                        methodVisitor.visitLabel(literalLabel);
                                        methodVisitor.visitFrame(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);

                                        return StackSize.SINGLE.toDecreasingSize();
                                    }
                                },
                                MethodVariableAccess.REFERENCE.loadOffset(0),
                                FieldAccess.forField(target.getInstrumentedType()
                                    .getDeclaredFields()
                                    .filter(ElementMatchers.named("visitor"))
                                    .getOnly()).getter(),
                                MethodVariableAccess.REFERENCE.loadOffset(1),
                                TypeCasting.to(new TypeDescription.ForLoadedType(LiteralNode.class)),
                                MethodInvocation.invoke(new TypeDescription.ForLoadedType(clazz)
                                    .getDeclaredMethods()
                                    .filter(ElementMatchers.named(methodName).and(ElementMatchers.takesArguments(LiteralNode.class)))
                                    .getOnly()),

                                new StackManipulation() {
                                    @Override
                                    public boolean isValid() {
                                        return true;
                                    }

                                    @Override
                                    public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
                                        methodVisitor.visitLabel(defaultLabel);
                                        methodVisitor.visitFrame(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);
                                        return StackSize.ZERO.toIncreasingSize();
                                    }
                                },

                                MethodReturn.VOID
                            ).apply(methodVisitor, context);
                            return new Size(size.getMaximalSize(), instrumentedMethod.getStackSize());
                        }
                    };
                }
            })
            .make()
            .load(DynamicNodeVisitor.class.getClassLoader())
            .getLoaded();

        return new DynamicNodeVisitor<T>(clazz, visitor);
    }

    private static boolean isVisitMethod(String methodName, Method method) {
        return
            method.getName().equals(methodName) &&
            method.getParameterCount() == 1 &&
            Node.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    private final Class<T> clazz;
    private final Class<? extends Consumer> visitor;

    public DynamicNodeVisitor(Class<T> clazz, Class<? extends Consumer> visitor) {
        this.clazz = clazz;
        this.visitor = visitor;
    }

    public Consumer<Node> instantiate(T instance) {
        try {
            return visitor.getConstructor(clazz).newInstance(instance);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }
}
