package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

public class FormalTypeParameterNode implements Node {
    public static FormalTypeParameterNode formalTypeParameter(TypeName name) {
        return new FormalTypeParameterNode(name);
    }

    private final TypeName name;

    public FormalTypeParameterNode(TypeName name) {
        this.name = name;
    }

    public TypeName getName() {
        return name;
    }

    public FormalTypeParameterNode transform(NodeTransformer transformer) {
        return new FormalTypeParameterNode(transformer.transform(name));
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "FormalTypeParameterNode(" +
            "name=" + name +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormalTypeParameterNode that = (FormalTypeParameterNode) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
