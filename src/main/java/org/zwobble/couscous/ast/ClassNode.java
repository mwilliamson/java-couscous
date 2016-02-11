package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;
import java.util.Set;

public class ClassNode implements Node {
    public static ClassNodeBuilder builder(TypeName name) {
        return new ClassNodeBuilder(name);
    }
    public static ClassNodeBuilder builder(String name) {
        return new ClassNodeBuilder(name);
    }
    public static ClassNode declareClass(
            TypeName name,
            Set<TypeName> superTypes,
            List<FieldDeclarationNode> fields,
            List<StatementNode> staticConstructor,
            ConstructorNode constructor,
            List<MethodNode> methods) {
        return new ClassNode(name, superTypes, fields, staticConstructor, constructor, methods);
    }
    
    private final TypeName name;
    private final Set<TypeName> superTypes;
    private final List<FieldDeclarationNode> fields;
    private final List<StatementNode> staticConstructor;
    private final ConstructorNode constructor;
    private final List<MethodNode> methods;
    
    private ClassNode(
        TypeName name,
        Set<TypeName> superTypes,
        List<FieldDeclarationNode> fields,
        List<StatementNode> staticConstructor,
        ConstructorNode constructor,
        List<MethodNode> methodNodes)
    {
        this.name = name;
        this.superTypes = superTypes;
        this.fields = fields;
        this.staticConstructor = staticConstructor;
        this.constructor = constructor;
        methods = methodNodes;
    }
    
    public TypeName getName() {
        return name;
    }

    public Set<TypeName> getSuperTypes() {
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
    
    public List<MethodNode> getMethods() {
        return methods;
    }
    
    public String getSimpleName() {
        return name.getSimpleName();
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    public ClassNode transform(NodeTransformer transformer) {
        return new ClassNode(
            transformer.transform(name),
            transformer.transformTypes(superTypes),
            transformer.transformFields(fields),
            staticConstructor, transformer.transformConstructor(constructor),
            transformer.transformMethods(methods));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassNode classNode = (ClassNode) o;

        if (!name.equals(classNode.name)) return false;
        if (!superTypes.equals(classNode.superTypes)) return false;
        if (!fields.equals(classNode.fields)) return false;
        if (!staticConstructor.equals(classNode.staticConstructor)) return false;
        if (!constructor.equals(classNode.constructor)) return false;
        return methods.equals(classNode.methods);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + superTypes.hashCode();
        result = 31 * result + fields.hashCode();
        result = 31 * result + staticConstructor.hashCode();
        result = 31 * result + constructor.hashCode();
        result = 31 * result + methods.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ClassNode(" +
            "name=" + name +
            ", superTypes=" + superTypes +
            ", fields=" + fields +
            ", staticConstructor=" + staticConstructor +
            ", constructor=" + constructor +
            ", methods=" + methods +
            ')';
    }
}
