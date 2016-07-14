package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

public interface StatementNode extends Node {
    <T> T accept(StatementNodeMapper<T> visitor);
    StatementNode transform(NodeTransformer transformer);
}
