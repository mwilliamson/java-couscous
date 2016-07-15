package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.util.ExtraIterables;

public class ReturnNode implements StatementNode {
    public static ReturnNode returns(ExpressionNode value) {
        return new ReturnNode(value);
    }
    
    private final ExpressionNode value;

    private ReturnNode(ExpressionNode value) {
        this.value = value;
    }
    
    public ExpressionNode getValue() {
        return value;
    }
    
    @Override
    public int type() {
        return NodeTypes.RETURN;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(value);
    }

    @Override
    public StatementNode transformSubtree(NodeTransformer transformer) {
        return new ReturnNode(transformer.transformExpression(value));
    }

    @Override
    public String toString() {
        return "ReturnNode(value=" + value + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        ReturnNode other = (ReturnNode) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
