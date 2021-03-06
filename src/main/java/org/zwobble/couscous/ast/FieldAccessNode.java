package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

import static org.zwobble.couscous.ast.InstanceReceiver.instanceReceiver;
import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;

public class FieldAccessNode implements AssignableExpressionNode {
    public static FieldAccessNode fieldAccess(
            ExpressionNode left,
            String fieldName,
            Type type)
    {
        return fieldAccess(instanceReceiver(left), fieldName, type);
    }

    public static FieldAccessNode fieldAccess(
        ScalarType left,
        String fieldName,
        Type type)
    {
        return fieldAccess(staticReceiver(left), fieldName, type);
    }

    public static FieldAccessNode fieldAccess(
        Receiver left,
        String fieldName,
        Type type)
    {
        return new FieldAccessNode(left, fieldName, type);
    }
    
    private final Receiver left;
    private final String fieldName;
    private final Type type;
    
    public FieldAccessNode(
        Receiver left,
        String fieldName,
        Type type)
    {
        this.left = left;
        this.fieldName = fieldName;
        this.type = type;
    }
    
    public Receiver getLeft() {
        return left;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Type getType() {
        return type;
    }
    
    @Override
    public int nodeType() {
        return NodeTypes.FIELD_ACCESS;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(left);
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
        return new FieldAccessNode(
            transformer.transformReceiver(left),
            transformer.transformFieldName(fieldName),
            transformer.transform(type));
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
    public boolean equals(Object obj) {
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
