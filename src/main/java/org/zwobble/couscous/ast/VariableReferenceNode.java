package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

public class VariableReferenceNode implements AssignableExpressionNode, ReferenceNode {
    public static VariableReferenceNode reference(VariableNode variable) {
        return new VariableReferenceNode(variable.getDeclaration());
    }
    
    public static VariableReferenceNode reference(VariableDeclaration referent) {
        return new VariableReferenceNode(referent);
    }
    
    private final VariableDeclaration referent;

    private VariableReferenceNode(VariableDeclaration referent) {
        this.referent = referent;
    }
    
    public VariableDeclaration getReferent() {
        return referent;
    }
    
    public Identifier getReferentId() {
        return referent.getId();
    }

    @Override
    public Type getType() {
        return referent.getType();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int nodeType() {
        return NodeTypes.VARIABLE_REFERENCE;
    }

    @Override
    public Iterable<Node> childNodes() {
        return ExtraIterables.empty();
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
        return new VariableReferenceNode(transformer.transform(referent));
    }

    @Override
    public String toString() {
        return "VariableReferenceNode(referent=" + referent + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((referent == null) ? 0 : referent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VariableReferenceNode other = (VariableReferenceNode) obj;
        if (referent == null) {
            if (other.referent != null)
                return false;
        } else if (!referent.equals(other.referent))
            return false;
        return true;
    }
}
