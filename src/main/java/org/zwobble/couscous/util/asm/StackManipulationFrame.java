package org.zwobble.couscous.util.asm;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class StackManipulationFrame implements StackManipulation {
    public static final StackManipulation SAME = new StackManipulationFrame();

    private StackManipulationFrame() {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);
        return StackSize.ZERO.toIncreasingSize();
    }
}
