package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.AssignableExpressionNodeVisitor;

public interface AssignableExpressionNode extends ExpressionNode {
    void accept(AssignableExpressionNodeVisitor mapper);
}
