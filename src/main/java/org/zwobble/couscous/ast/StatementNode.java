package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;

public interface StatementNode extends Node {
    StatementNode transform(NodeTransformer transformer);
}
