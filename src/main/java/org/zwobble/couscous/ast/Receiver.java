package org.zwobble.couscous.ast;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

public interface Receiver extends Node {
    <T> T accept(Mapper<T> mapper);
    Receiver transformSubtree(NodeTransformer transformer);

    interface Mapper<T> {
        T visit(ExpressionNode receiver);
        T visit(ScalarType receiver);
    }
}
