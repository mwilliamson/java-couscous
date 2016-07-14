package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;

public interface ExpressionNode extends Node {
    Type getType();
    <T> T accept(ExpressionNodeMapper<T> visitor);


    ExpressionNode transform(NodeTransformer transformer);
}
