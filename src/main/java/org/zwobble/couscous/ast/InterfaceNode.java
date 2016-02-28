package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;

import java.util.List;
import java.util.Set;

public class InterfaceNode implements TypeNode {
    public static InterfaceNode declareInterface(
        TypeName name,
        Set<TypeName> superTypes,
        List<MethodNode> methods)
    {
        return new InterfaceNode(name, superTypes, methods);
    }

    private final TypeName name;
    private final Set<TypeName> superTypes;
    private final List<MethodNode> methods;

    private InterfaceNode(
        TypeName name,
        Set<TypeName> superTypes,
        List<MethodNode> methodNodes)
    {
        this.name = name;
        this.superTypes = superTypes;
        methods = methodNodes;
    }

    @Override
    public TypeName getName() {
        return name;
    }

    @Override
    public Set<TypeName> getSuperTypes() {
        return superTypes;
    }

    @Override
    public List<MethodNode> getMethods() {
        return methods;
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        // TODO: implement this
        return null;
    }

    @Override
    public String toString() {
        return "InterfaceNode(" +
            "name=" + name +
            ", superTypes=" + superTypes +
            ", methods=" + methods +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterfaceNode that = (InterfaceNode) o;

        if (!name.equals(that.name)) return false;
        if (!superTypes.equals(that.superTypes)) return false;
        return methods.equals(that.methods);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + superTypes.hashCode();
        result = 31 * result + methods.hashCode();
        return result;
    }
}
