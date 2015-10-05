package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;
import org.zwobble.couscous.values.TypeReference;

public interface ExpressionNode {
    TypeReference getType();
    <T> T accept(ExpressionNodeVisitor<T> visitor);
}
