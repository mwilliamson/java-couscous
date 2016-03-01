package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;
import java.util.Set;

public interface TypeNode extends Node {
    TypeName getName();
    Set<TypeName> getSuperTypes();
    List<MethodNode> getMethods();
    TypeNode transform(NodeTransformer transformer);
}
