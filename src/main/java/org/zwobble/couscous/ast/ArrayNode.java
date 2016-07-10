package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

import java.util.List;

public class ArrayNode implements ExpressionNode {
    public static ArrayNode array(Type elementType, List<ExpressionNode> expressions) {
        return new ArrayNode(elementType, expressions);
    }

    private final Type elementType;
    private final List<ExpressionNode> elements;

    public ArrayNode(Type elementType, List<ExpressionNode> elements) {
        this.elementType = elementType;
        this.elements = elements;
    }

    public Type getElementType() {
        return elementType;
    }

    public List<ExpressionNode> getElements() {
        return elements;
    }

    @Override
    public Type getType() {
        return Types.array(elementType);
    }

    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int type() {
        return NodeTypes.ARRAY;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return elements;
    }

    @Override
    public ExpressionNode transform(NodeTransformer transformer) {
        return new ArrayNode(
            transformer.transform(elementType),
            transformer.transformExpressions(elements));
    }

    @Override
    public String toString() {
        return "ArrayNode(" +
            "elementType=" + elementType +
            ", elements=" + elements +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayNode arrayNode = (ArrayNode) o;

        if (!elementType.equals(arrayNode.elementType)) return false;
        return elements.equals(arrayNode.elements);

    }

    @Override
    public int hashCode() {
        int result = elementType.hashCode();
        result = 31 * result + elements.hashCode();
        return result;
    }
}
