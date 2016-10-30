package org.zwobble.couscous.transforms;

import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;

import java.util.*;

import static org.zwobble.couscous.util.ExtraLists.eagerFilter;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.ExtraSets.set;

public class SplitStaticsFromInterface {
    public static List<TypeNode> transform(List<TypeNode> typeNodes) {
        List<TypeNode> result = new ArrayList<>();
        Set<ScalarType> splitInterfaces = new HashSet<>();

        for (TypeNode typeNode : typeNodes) {
            if (isSplittable(typeNode)) {
                splitInterfaces.add(typeNode.getName());
                InterfaceNode interfaceNode = (InterfaceNode) typeNode;
                result.add(ClassNode.declareClass(
                    staticTypeFor(interfaceNode.getName()),
                    list(),
                    set(),
                    interfaceNode.getFields(),
                    interfaceNode.getStaticConstructor(),
                    ConstructorNode.DEFAULT,
                    eagerFilter(interfaceNode.getMethods(), method -> method.isStatic()),
                    list()
                ));
                result.add(InterfaceNode.declareInterface(
                    interfaceNode.getName(),
                    interfaceNode.getTypeParameters(),
                    interfaceNode.getSuperTypes(),
                    list(),
                    list(),
                    eagerFilter(interfaceNode.getMethods(), method -> !method.isStatic()),
                    interfaceNode.getInnerTypes()
                ));
            } else {
                result.add(typeNode);
            }
        }

        NodeTransformer transformer = NodeTransformer.builder()
            .transformReceiver((Receiver receiver) -> {
                if (receiver instanceof StaticReceiver && splitInterfaces.contains(((StaticReceiver) receiver).getType())) {
                    return StaticReceiver.staticReceiver(staticTypeFor(((StaticReceiver) receiver).getType()));
                } else {
                    return receiver;
                }
            })
            .build();
        return NodeTransformer.apply(transformer, result);
    }

    private static ScalarType staticTypeFor(ScalarType name) {
        return ScalarType.topLevel(name.getQualifiedName() + "_static");
    }

    private static boolean isSplittable(TypeNode typeNode) {
        if (!(typeNode instanceof InterfaceNode)) {
            return false;
        }
        InterfaceNode interfaceNode = (InterfaceNode) typeNode;
        return !interfaceNode.getFields().isEmpty() || Iterables.any(interfaceNode.getMethods(), MethodNode::isStatic);
    }
}
