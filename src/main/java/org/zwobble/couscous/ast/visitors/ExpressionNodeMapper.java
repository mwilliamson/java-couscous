package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.*;

public interface ExpressionNodeMapper<T> {
    T visit(LiteralNode literal);
    T visit(VariableReferenceNode variableReference);
    T visit(ThisReferenceNode reference);
    T visit(AssignmentNode assignment);
    T visit(TernaryConditionalNode ternaryConditional);
    T visit(MethodCallNode methodCall);
    T visit(ConstructorCallNode call);
    T visit(FieldAccessNode fieldAccess);
    T visit(TypeCoercionNode typeCoercion);
    T visit(CastNode cast);
}
