package org.zwobble.couscous;

import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import org.zwobble.couscous.interpreter.values.IntegerInterpreterValue;

import com.google.common.collect.Iterables;

import lombok.val;

public class JavaProject {
    public static MapBackedProject.Builder builder() {
        return MapBackedProject.builder()
            .addClass(IntegerInterpreterValue.TYPE);
    }

    public static Project of(List<ClassNode> classNodes) {
        val concreteTypes = Iterables.transform(classNodes, ConcreteType::fromNode);
        return builder()
                .addClasses(concreteTypes)
                .build();
    }
}
