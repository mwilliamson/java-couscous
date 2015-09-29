package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.Assignment;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.VariableReferenceNode;

public interface ExpressionNodeVisitor<T> {
    T visit(LiteralNode literal);
    T visit(VariableReferenceNode variableReference);
    T visit(Assignment assignment);
    T visit(TernaryConditionalNode ternaryConditional);
    T visit(MethodCallNode methodCall);
}
