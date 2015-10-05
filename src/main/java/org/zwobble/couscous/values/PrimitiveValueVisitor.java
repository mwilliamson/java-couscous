package org.zwobble.couscous.values;

public interface PrimitiveValueVisitor<T> {
    T visit(IntegerValue value);
    T visit(StringValue value);
    T visit(BooleanValue value);
    T visit(UnitValue unitValue);
}
