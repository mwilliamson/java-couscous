package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;

public class AssignmentNode implements ExpressionNode {
    public static StatementNode assignStatement(AssignableExpressionNode target, ExpressionNode value) {
        return expressionStatement(assign(target, value));
    }
    
    public static AssignmentNode assign(AssignableExpressionNode target, ExpressionNode value) {
        return new AssignmentNode(target, value);
    }
    
    public static StatementNode assignStatement(VariableNode target, ExpressionNode value) {
        return expressionStatement(assign(target, value));
    }
    
    public static AssignmentNode assign(VariableNode target, ExpressionNode value) {
        return new AssignmentNode(reference(target), value);
    }
    
    private final AssignableExpressionNode target;
    private final ExpressionNode value;
    
    private AssignmentNode(AssignableExpressionNode target, ExpressionNode value) {
        this.target = target;
        this.value = value;
    }
    
    public AssignableExpressionNode getTarget() {
        return target;
    }
    
    public ExpressionNode getValue() {
        return value;
    }
    
    @Override
    public int nodeType() {
        return NodeTypes.ASSIGNMENT;
    }

    @Override
    public Iterable<Node> childNodes() {
        return ExtraIterables.of(value, target);
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
        return new AssignmentNode(
            (AssignableExpressionNode) transformer.transformExpression(target),
            transformer.transformExpression(value));
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public String toString() {
        return "AssignmentNode(target=" + target + ", value=" + value + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        AssignmentNode other = (AssignmentNode) obj;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
