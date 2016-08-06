package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;

public interface Node {
    int nodeType();
    Iterable<? extends Node> childNodes();
    Node transformSubtree(NodeTransformer transformer);
}
