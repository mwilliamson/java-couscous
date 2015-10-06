package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.AssignmentNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.VariableReferenceNode;

public interface ExpressionNodeMapper<T> {
    T visit(LiteralNode literal);
    T visit(VariableReferenceNode variableReference);
    T visit(AssignmentNode assignment);
    T visit(TernaryConditionalNode ternaryConditional);
    T visit(MethodCallNode methodCall);
    T visit(StaticMethodCallNode staticMethodCall);
    T visit(ConstructorCallNode call);
}
