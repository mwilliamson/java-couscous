package org.zwobble.couscous.interpreter;

import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.interpreter.values.BoxedIntegerInterpreterValue;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import org.zwobble.couscous.interpreter.values.InternalCouscousInterpreterValue;

import com.google.common.collect.Iterables;

public class JavaProject {
    private static final ConcreteType OBJECT_TYPE = ConcreteType.classBuilder("java.lang.Object")
        .build();
    
    public static MapBackedProject.Builder builder() {
        return MapBackedProject.builder()
            .addClass(InternalCouscousInterpreterValue.TYPE)
            .addClass(OBJECT_TYPE)
            .addClass(BoxedIntegerInterpreterValue.TYPE);
    }

    public static Project of(List<TypeNode> classNodes) {
        Iterable<ConcreteType> concreteTypes = Iterables.transform(classNodes, ConcreteType::fromNode);
        return builder()
                .addClasses(concreteTypes)
                .build();
    }
}
