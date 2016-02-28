package org.zwobble.couscous.interpreter;

import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.interpreter.types.UserDefinedInterpreterType;
import org.zwobble.couscous.interpreter.values.BoxedIntegerInterpreterValue;
import org.zwobble.couscous.interpreter.values.InternalCouscousInterpreterValue;

import java.util.List;

public class JavaProject {
    private static final InterpreterType OBJECT_TYPE = IntrinsicInterpreterType.classBuilder("java.lang.Object")
        .build();
    
    public static MapBackedProject.Builder builder() {
        return MapBackedProject.builder()
            .addClass(InternalCouscousInterpreterValue.TYPE)
            .addClass(OBJECT_TYPE)
            .addClass(BoxedIntegerInterpreterValue.TYPE);
    }

    public static Project of(List<TypeNode> classNodes) {
        Iterable<InterpreterType> concreteTypes = Iterables.transform(classNodes, UserDefinedInterpreterType::new);
        return builder()
                .addClasses(concreteTypes)
                .build();
    }
}
