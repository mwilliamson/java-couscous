package org.zwobble.couscous.ast;

import org.zwobble.couscous.types.ParameterizedType;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.values.*;

import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public class LiteralNode implements ExpressionNode {
    public static final ExpressionNode UNIT = literal(PrimitiveValues.UNIT);

    public static LiteralNode literal(PrimitiveValue value) {
        return new LiteralNode(value, value.getType());
    }
    
    public static LiteralNode literal(String value) {
        return new LiteralNode(value(value), StringValue.REF);
    }
    
    public static LiteralNode literal(int value) {
        return new LiteralNode(value(value), IntegerValue.REF);
    }
    
    public static LiteralNode literal(boolean value) {
        return new LiteralNode(value(value), BooleanValue.REF);
    }

    public static LiteralNode literal(ScalarType type) {
        return new LiteralNode(value(type), new ParameterizedType(ObjectValues.CLASS, list(type)));
    }
    
    public static LiteralNode of(PrimitiveValue value, Type type) {
        return new LiteralNode(value, type);
    }
    
    private final PrimitiveValue value;
    private final Type type;

    private LiteralNode(PrimitiveValue value, Type type) {
        this.value = value;
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
    
    public PrimitiveValue getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ExpressionNode transform(NodeTransformer transformer) {
        return new LiteralNode(transformValue(transformer), transformer.transform(type));
    }

    private PrimitiveValue transformValue(NodeTransformer transformer) {
        if (value instanceof TypeValue) {
            return new TypeValue(transformer.transform(((TypeValue)value).getValue()));
        } else {
            return value;
        }
    }

    @Override
    public String toString() {
        return "LiteralNode(value=" + value + ", type=" + type + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        LiteralNode other = (LiteralNode) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
