package org.zwobble.couscous.ast;

public interface ReferenceNode extends ExpressionNode {
    interface Visitor<T> {
        T visit(VariableReferenceNode variableReference);
        T visit(ThisReferenceNode thisReference);
    }

    <T> T accept(Visitor<T> visitor);
}
