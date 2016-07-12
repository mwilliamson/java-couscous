package org.zwobble.couscous.util.asm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;

import java.util.List;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.util.ExtraIterables.lazyMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class StackManipulationSwitch implements StackManipulation {
    public static Case switchCase(int key, StackManipulation stackManipulation) {
        return new Case(key, stackManipulation);
    }

    public static class Case {
        private final int key;
        private final StackManipulation stackManipulation;

        private Case(int key, StackManipulation stackManipulation) {
            this.key = key;
            this.stackManipulation = stackManipulation;
        }
    }

    private final StackManipulation defaultCase;
    private final List<Case> cases;

    public StackManipulationSwitch(StackManipulation defaultCase, List<Case> cases) {
        this.defaultCase = defaultCase;
        this.cases = Ordering.<Case>from((first, second) -> first.key - second.key).immutableSortedCopy(cases);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
        Label defaultLabel = new Label();
        Label[] caseLabels = Iterables.toArray(lazyMap(cases, switchCase -> new Label()), Label.class);

        return new StackManipulation.Compound(ImmutableList.copyOf(Iterables.concat(
            list(
                new StackManipulationLookupTable(
                    defaultLabel,
                    cases.stream().mapToInt(switchCase -> switchCase.key).toArray(),
                    caseLabels
                )
            ),

            lazyMap(cases, asList(caseLabels), (switchCase, label) -> new StackManipulation.Compound(
                new StackManipulationLabel(label),
                StackManipulationFrame.SAME,
                switchCase.stackManipulation
            )),

            list(
                new StackManipulationLabel(defaultLabel),
                StackManipulationFrame.SAME,
                defaultCase
            )
        ))).apply(methodVisitor, implementationContext);
    }
}
