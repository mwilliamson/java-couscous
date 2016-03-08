package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;
import java.util.Set;

public class InterfaceNode implements TypeNode {
    public static InterfaceNode declareInterface(
        TypeName name,
        List<FormalTypeParameterNode> typeParameters,
        Set<TypeName> superTypes,
        List<MethodNode> methods)
    {
        return new InterfaceNode(name, typeParameters, superTypes, methods);
    }

    private final TypeName name;
    private final List<FormalTypeParameterNode> typeParameters;
    private final Set<TypeName> superTypes;
    private final List<MethodNode> methods;

    private InterfaceNode(
        TypeName name,
        List<FormalTypeParameterNode> typeParameters,
        Set<TypeName> superTypes,
        List<MethodNode> methodNodes)
    {
        this.name = name;
        this.typeParameters = typeParameters;
        this.superTypes = superTypes;
        methods = methodNodes;
    }

    @Override
    public TypeName getName() {
        return name;
    }

    @Override
    public List<FormalTypeParameterNode> getTypeParameters() {
        return typeParameters;
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
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "InterfaceNode(" +
            "name=" + name +
            ", typeParameters=" + typeParameters +
            ", superTypes=" + superTypes +
            ", methods=" + methods +
            ')';
    }

    @Override
    public InterfaceNode transform(NodeTransformer transformer) {
        return new InterfaceNode(
            transformer.transform(name),
            transformer.transformFormalTypeParameters(typeParameters),
            transformer.transformTypes(superTypes),
            transformer.transformMethods(methods));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterfaceNode that = (InterfaceNode) o;

        if (!name.equals(that.name)) return false;
        if (!typeParameters.equals(that.typeParameters)) return false;
        if (!superTypes.equals(that.superTypes)) return false;
        return methods.equals(that.methods);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeParameters.hashCode();
        result = 31 * result + superTypes.hashCode();
        result = 31 * result + methods.hashCode();
        return result;
    }
}
