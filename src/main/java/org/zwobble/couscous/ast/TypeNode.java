package org.zwobble.couscous.ast;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;
import java.util.Set;

public interface TypeNode extends Node {
    ScalarType getName();
    List<FormalTypeParameterNode> getTypeParameters();
    Set<Type> getSuperTypes();
    List<MethodNode> getMethods();
    TypeNode transformSubtree(NodeTransformer transformer);
}
