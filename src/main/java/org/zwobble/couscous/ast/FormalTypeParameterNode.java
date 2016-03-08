package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;

public class FormalTypeParameterNode implements Node {
    public static FormalTypeParameterNode formalTypeParameter(String name) {
        return new FormalTypeParameterNode(name);
    }

    private final String name;

    public FormalTypeParameterNode(String name) {
        this.name = name;
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
