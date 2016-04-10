package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Set;

import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.ExtraSets.set;

public class EnumNode implements TypeNode {
    public static EnumNode declareEnum(ScalarType name) {
        return new EnumNode(name);
    }

    private final ScalarType name;

    public EnumNode(ScalarType name) {
        this.name = name;
    }

    @Override
    public ScalarType getName() {
        return name;
    }

    @Override
    public List<FormalTypeParameterNode> getTypeParameters() {
        return list();
    }

    @Override
    public Set<Type> getSuperTypes() {
        return set();
    }

    @Override
    public List<MethodNode> getMethods() {
        return list();
    }

    @Override
    public TypeNode transform(NodeTransformer transformer) {
        return new EnumNode(transformer.transform(name));
    }

    @Override
    public String toString() {
        return "EnumNode(" +
            "name=" + name +
            ')';
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnumNode enumNode = (EnumNode) o;

        return name.equals(enumNode.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
