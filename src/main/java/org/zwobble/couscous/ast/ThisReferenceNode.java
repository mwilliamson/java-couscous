package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.function.Function;

public class ThisReferenceNode implements ExpressionNode, ReferenceNode {
    public static ThisReferenceNode thisReference(TypeName type) {
        return new ThisReferenceNode(type);
    }
    
    private final TypeName type;

    private ThisReferenceNode(TypeName type) {
        this.type = type;
    }
    
    public TypeName getType() {
        return type;
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ExpressionNode replaceExpressions(Function<ExpressionNode, ExpressionNode> replace) {
        return this;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public ExpressionNode transform(NodeTransformer transformer) {
        return new ThisReferenceNode(transformer.transform(type));
    }

    @Override
    public String toString() {
        return "ThisReferenceNode(type=" + type + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        ThisReferenceNode other = (ThisReferenceNode) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
