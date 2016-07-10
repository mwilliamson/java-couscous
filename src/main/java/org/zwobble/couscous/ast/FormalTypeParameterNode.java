package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.TypeParameter;
import org.zwobble.couscous.util.ExtraIterables;

public class FormalTypeParameterNode implements Node {
    public static FormalTypeParameterNode formalTypeParameter(Identifier declaringScope, String name) {
        return new FormalTypeParameterNode(new TypeParameter(declaringScope, name));
    }

    public static FormalTypeParameterNode formalTypeParameter(TypeParameter type) {
        return new FormalTypeParameterNode(type);
    }

    private final TypeParameter type;

    public FormalTypeParameterNode(TypeParameter type) {
        this.type = type;
    }

    public TypeParameter getType() {
        return type;
    }

    public String getName() {
        return type.getName();
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.empty();
    }

    public FormalTypeParameterNode transform(NodeTransformer transformer) {
        return new FormalTypeParameterNode(transformer.transform(type));
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FormalTypeParameterNode(" +
            "type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormalTypeParameterNode that = (FormalTypeParameterNode) o;

        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
