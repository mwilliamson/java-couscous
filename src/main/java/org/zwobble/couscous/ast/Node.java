package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;

public interface Node {
    int type();
    Iterable<? extends Node> childNodes();
    Node transform(NodeTransformer transformer);
}
