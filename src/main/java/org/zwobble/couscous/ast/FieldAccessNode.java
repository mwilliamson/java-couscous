package org.zwobble.couscous.ast;

import javax.annotation.Nullable;

import org.zwobble.couscous.ast.visitors.AssignableExpressionNodeVisitor;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

public class FieldAccessNode implements AssignableExpressionNode {
    public static FieldAccessNode fieldAccess(
            ExpressionNode left,
            String fieldName,
            TypeName type) {
        return new FieldAccessNode(left, fieldName, type);
    }
    
    private final ExpressionNode left;
    private final String fieldName;
    private final TypeName type;
    
    public FieldAccessNode(
            ExpressionNode left,
            String fieldName,
            TypeName type) {
        this.left = left;
        this.fieldName = fieldName;
        this.type = type;
    }
    
    public ExpressionNode getLeft() {
        return left;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public TypeName getType() {
        return type;
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(AssignableExpressionNodeVisitor mapper) {
        mapper.visit(this);
    }

    @Override
    public String toString() {
        return "FieldAccessNode(left=" + left + ", fieldName=" + fieldName
               + ", type=" + type + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldAccessNode other = (FieldAccessNode) obj;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        if (left == null) {
            if (other.left != null)
                return false;
        } else if (!left.equals(other.left))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
