package org.zwobble.couscous.ast;

import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

import java.util.List;
import java.util.Set;

import static org.zwobble.couscous.util.ExtraLists.eagerFlatMap;

public class ClassNode implements TypeNode {
    public static ClassNodeBuilder builder(ScalarType name) {
        return new ClassNodeBuilder(name);
    }
    public static ClassNodeBuilder builder(String name) {
        return new ClassNodeBuilder(name);
    }

    public static ClassNode declareClass(
        ScalarType name,
        List<FormalTypeParameterNode> typeParameters,
        Set<Type> superTypes,
        List<FieldDeclarationNode> fields,
        List<StatementNode> staticConstructor,
        ConstructorNode constructor,
        List<MethodNode> methods,
        List<TypeNode> innerTypes
    )
    {
        return new ClassNode(name, typeParameters, superTypes, fields, staticConstructor, constructor, methods, innerTypes);
    }
    
    private final ScalarType name;
    private final List<FormalTypeParameterNode> typeParameters;
    private final Set<Type> superTypes;
    private final List<FieldDeclarationNode> fields;
    private final List<StatementNode> staticConstructor;
    private final ConstructorNode constructor;
    private final List<MethodNode> methods;
    private final List<TypeNode> innerTypes;

    public ClassNode(
        ScalarType name,
        List<FormalTypeParameterNode> typeParameters,
        Set<Type> superTypes,
        List<FieldDeclarationNode> fields,
        List<StatementNode> staticConstructor,
        ConstructorNode constructor,
        List<MethodNode> methodNodes,
        List<TypeNode> innerTypes
    )
    {
        this.name = name;
        this.typeParameters = typeParameters;
        this.superTypes = superTypes;
        this.fields = fields;
        this.staticConstructor = staticConstructor;
        this.constructor = constructor;
        methods = methodNodes;
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

    public ConstructorNode getConstructor() {
        return constructor;
    }

    @Override
    public List<MethodNode> getMethods() {
        return methods;
    }

    public List<TypeNode> getInnerTypes() {
        return innerTypes;
    }

    public String getSimpleName() {
        return name.getSimpleName();
    }

    @Override
    public int nodeType() {
        return NodeTypes.CLASS;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return Iterables.concat(
            ExtraIterables.of(constructor),
            fields,
            methods
        );
    }

    @Override
    public ClassNode transformSubtree(NodeTransformer transformer) {
        return new ClassNode(
            transformer.transform(name),
            transformer.transformFormalTypeParameters(typeParameters),
            transformer.transformTypes(superTypes),
            transformer.transformFields(fields),
            transformer.transformStatements(staticConstructor),
            transformer.transformConstructor(constructor),
            transformer.transformMethods(methods),
            eagerFlatMap(innerTypes, transformer::transformTypeDeclaration)
        );
    }

    @Override
    public TypeNode rename(ScalarType name) {
        return new ClassNode(
            name,
            typeParameters,
            superTypes,
            fields,
            staticConstructor,
            constructor,
            methods,
            innerTypes
        );
    }

    @Override
    public String toString() {
        return "ClassNode(" +
            "name=" + name +
            ", typeParameters=" + typeParameters +
            ", superTypes=" + superTypes +
            ", fields=" + fields +
            ", staticConstructor=" + staticConstructor +
            ", constructor=" + constructor +
            ", methods=" + methods +
            ", innerTypes=" + innerTypes +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassNode classNode = (ClassNode) o;

        if (!name.equals(classNode.name)) return false;
        if (!typeParameters.equals(classNode.typeParameters)) return false;
        if (!superTypes.equals(classNode.superTypes)) return false;
        if (!fields.equals(classNode.fields)) return false;
        if (!staticConstructor.equals(classNode.staticConstructor)) return false;
        if (!constructor.equals(classNode.constructor)) return false;
        if (!methods.equals(classNode.methods)) return false;
        return innerTypes.equals(classNode.innerTypes);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeParameters.hashCode();
        result = 31 * result + superTypes.hashCode();
        result = 31 * result + fields.hashCode();
        result = 31 * result + staticConstructor.hashCode();
        result = 31 * result + constructor.hashCode();
        result = 31 * result + methods.hashCode();
        result = 31 * result + innerTypes.hashCode();
        return result;
    }
}
