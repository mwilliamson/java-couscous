package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;

public interface StatementNode extends Node {
    StatementNode transformSubtree(NodeTransformer transformer);
}
