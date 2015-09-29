package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;

public interface ExpressionNode {
    <T> T accept(ExpressionNodeVisitor<T> visitor);
}
