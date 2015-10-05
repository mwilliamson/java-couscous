package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeVisitor;

public interface Node {
    void accept(NodeVisitor visitor);
}
