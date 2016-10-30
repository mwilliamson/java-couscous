package org.zwobble.couscous.ast;

import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Set;

import static org.zwobble.couscous.util.ExtraLists.concat;
import static org.zwobble.couscous.util.ExtraLists.eagerFlatMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class InterfaceNode implements TypeNode {
    public static InterfaceNode declareInterface(
        ScalarType name,
        List<FormalTypeParameterNode> typeParameters,
        Set<Type> superTypes,
        List<FieldDeclarationNode> fields,
        List<StatementNode> staticConstructor,
        List<MethodNode> methods,
        List<TypeNode> innerTypes
    )
    {
        return new InterfaceNode(name, typeParameters, superTypes, fields, staticConstructor, methods, innerTypes);
    }

    private final ScalarType name;
    private final List<FormalTypeParameterNode> typeParameters;
    private final Set<Type> superTypes;
    private final List<FieldDeclarationNode> fields;
    private final List<StatementNode> staticConstructor;
    private final List<MethodNode> methods;
    private final List<TypeNode> innerTypes;

    private InterfaceNode(
        ScalarType name,
        List<FormalTypeParameterNode> typeParameters,
        Set<Type> superTypes,
        List<FieldDeclarationNode> fields,
        List<StatementNode> staticConstructor,
        List<MethodNode> methods,
        List<TypeNode> innerTypes
    )
    {
        this.name = name;
        this.typeParameters = typeParameters;
        this.superTypes = superTypes;
        this.fields = fields;
        this.staticConstructor = staticConstructor;
        this.methods = methods;
        this.innerTypes = innerTypes;
    }

    @Override
    public ScalarType getName() {
        return name;
    }

    @Override
    public List<FormalTypeParameterNode> getTypeParameters() {
        return typeParameters;
    }

    @Override
    public Set<Type> getSuperTypes() {
        return superTypes;
    }

    public List<FieldDeclarationNode> getFields() {
        return fields;
    }

    public List<StatementNode> getStaticConstructor() {
        return staticConstructor;
    }

    @Override
    public List<MethodNode> getMethods() {
        return methods;
    }

    @Override
    public List<TypeNode> getInnerTypes() {
        return innerTypes;
    }

    @Override
    public int nodeType() {
        return NodeTypes.INTERFACE;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return Iterables.concat(
            typeParameters,
            staticConstructor,
            fields,
            methods,
            innerTypes
        );
    }

    @Override
    public InterfaceNode transformSubtree(NodeTransformer transformer) {
        return new InterfaceNode(
            transformer.transform(name),
            transformer.transformFormalTypeParameters(typeParameters),
            transformer.transformTypes(superTypes),
            transformer.transformFields(fields),
            transformer.transformStatements(staticConstructor),
            transformer.transformMethods(methods),
            eagerFlatMap(innerTypes, transformer::transformTypeDeclaration)
        );
    }

    @Override
    public TypeNode stripInnerTypes() {
        return withInnerTypes(list());
    }

    @Override
    public TypeNode addInnerTypes(List<TypeNode> types) {
        return withInnerTypes(concat(innerTypes, types));
    }

    private TypeNode withInnerTypes(List<TypeNode> types) {
        return new InterfaceNode(
            name,
            typeParameters,
            superTypes,
            fields,
            staticConstructor,
            methods,
            types
        );
    }

    @Override
    public String toString() {
        return "InterfaceNode(" +
            "name=" + name +
            ", typeParameters=" + typeParameters +
            ", superTypes=" + superTypes +
            ", fields=" + fields +
            ", staticConstructor=" + staticConstructor +
            ", methods=" + methods +
            ", innerTypes=" + innerTypes +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterfaceNode that = (InterfaceNode) o;

        if (!name.equals(that.name)) return false;
        if (!typeParameters.equals(that.typeParameters)) return false;
        if (!superTypes.equals(that.superTypes)) return false;
        if (!fields.equals(that.fields)) return false;
        if (!staticConstructor.equals(that.staticConstructor)) return false;
        if (!methods.equals(that.methods)) return false;
        return innerTypes.equals(that.innerTypes);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeParameters.hashCode();
        result = 31 * result + superTypes.hashCode();
        result = 31 * result + fields.hashCode();
        result = 31 * result + staticConstructor.hashCode();
        result = 31 * result + methods.hashCode();
        result = 31 * result + innerTypes.hashCode();
        return result;
    }
}
