package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;

public interface ExpressionNode extends Node {
    Type getType();
    ExpressionNode transformSubtree(NodeTransformer transformer);
}
