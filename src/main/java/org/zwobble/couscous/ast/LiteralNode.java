package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;
import org.zwobble.couscous.values.TypeReference;

import static org.zwobble.couscous.values.PrimitiveValues.value;

import lombok.Value;

@Value(staticConstructor="of")
public class LiteralNode implements ExpressionNode {
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
    
    PrimitiveValue value;
    TypeReference type;

    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }
}
