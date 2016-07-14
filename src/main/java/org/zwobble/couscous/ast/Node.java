package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;

public interface Node {
    int type();
    Iterable<? extends Node> childNodes();
    <T> T accept(NodeMapper<T> visitor);
}
