package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

import java.util.List;
import java.util.Set;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.ExtraSets.set;

public class EnumNode implements TypeNode {
    public static EnumNode declareEnum(ScalarType name, List<String> values) {
        return new EnumNode(name, values);
    }

    private final ScalarType name;
    private final List<String> values;

    public EnumNode(ScalarType name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    @Override
    public ScalarType getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
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
    public int type() {
        return NodeTypes.ENUM;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.empty();
    }

    @Override
    public TypeNode transformSubtree(NodeTransformer transformer) {
        return new EnumNode(
            transformer.transform(name),
            eagerMap(values, transformer::transformFieldName));
    }

    @Override
    public String toString() {
        return "EnumNode(" +
            "name=" + name +
            ", values=" + values +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnumNode enumNode = (EnumNode) o;

        if (!name.equals(enumNode.name)) return false;
        return values.equals(enumNode.values);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }
}
