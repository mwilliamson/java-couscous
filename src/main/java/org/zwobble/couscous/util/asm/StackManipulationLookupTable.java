package org.zwobble.couscous.util.asm;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;

public class StackManipulationLookupTable implements StackManipulation {
    private final Label dflt;
    private final int[] keys;
    private final Label[] labels;

    public StackManipulationLookupTable(Label dflt, int[] keys, Label[] labels) {
        this.dflt = dflt;
        this.keys = keys;
        this.labels = labels;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
        methodVisitor.visitLookupSwitchInsn(dflt, keys, labels);
        return StackSize.SINGLE.toDecreasingSize();
    }
}
