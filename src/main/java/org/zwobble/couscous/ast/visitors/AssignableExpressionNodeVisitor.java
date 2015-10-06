package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.FieldAccessNode;
import org.zwobble.couscous.ast.VariableReferenceNode;

public interface AssignableExpressionNodeVisitor {
    void visit(VariableReferenceNode reference);
    void visit(FieldAccessNode fieldAccess);
}
