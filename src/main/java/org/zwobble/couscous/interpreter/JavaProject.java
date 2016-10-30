package org.zwobble.couscous.interpreter;

import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.interpreter.types.InterpreterType;
import org.zwobble.couscous.interpreter.types.IntrinsicInterpreterType;
import org.zwobble.couscous.interpreter.types.UserDefinedInterpreterType;
import org.zwobble.couscous.interpreter.values.BoxedIntegerInterpreterValue;
import org.zwobble.couscous.interpreter.values.InternalCouscousInterpreterValue;
import org.zwobble.couscous.transforms.AnonymousClassToInnerClass;
import org.zwobble.couscous.transforms.DesugarForToWhile;
import org.zwobble.couscous.transforms.DesugarSwitchToIfElse;
import org.zwobble.couscous.transforms.HoistNestedTypes;

import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.*;

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
        NodeTransformer switchTransformer = DesugarSwitchToIfElse.transformer();
        NodeTransformer forTransformer = DesugarForToWhile.transformer();
        Iterable<InterpreterType> concreteTypes = Iterables.transform(
            HoistNestedTypes.hoist(
                eagerMap(
                    NodeTransformer.applyAll(list(switchTransformer, forTransformer), classNodes),
                    AnonymousClassToInnerClass::transform
                )
            ),
            UserDefinedInterpreterType::new
        );
        return builder()
                .addClasses(concreteTypes)
                .build();
    }
}
