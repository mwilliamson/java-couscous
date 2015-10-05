package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;
import org.zwobble.couscous.values.PrimitiveValue;

import static org.zwobble.couscous.values.PrimitiveValues.value;

import lombok.Value;

@Value
public class LiteralNode implements ExpressionNode {
    public static LiteralNode literal(PrimitiveValue value) {
        return new LiteralNode(value);
    }
    
    public static LiteralNode literal(String value) {
        return new LiteralNode(value(value));
    }
    
    public static LiteralNode literal(int value) {
        return new LiteralNode(value(value));
    }
    
    public static LiteralNode literal(boolean value) {
        return new LiteralNode(value(value));
    }
    
    PrimitiveValue value;

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
