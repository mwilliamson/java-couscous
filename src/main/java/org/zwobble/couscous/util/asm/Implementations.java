package org.zwobble.couscous.util.asm;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;

import java.util.function.Function;

public class Implementations {
    public static Implementation stackManipulation(Function<Implementation.Target, StackManipulation> build) {
        return new Implementation() {
            @Override
            public InstrumentedType prepare(InstrumentedType instrumentedType) {
                return instrumentedType;
            }

            @Override
            public ByteCodeAppender appender(Target implementationTarget) {
                return new ByteCodeAppender() {
                    @Override
                    public Size apply(MethodVisitor methodVisitor, Context implementationContext, MethodDescription instrumentedMethod) {
                        return new ByteCodeAppender.Simple(build.apply(implementationTarget))
                            .apply(methodVisitor, implementationContext, instrumentedMethod);
                    }
                };
            }
        };
    }
}
