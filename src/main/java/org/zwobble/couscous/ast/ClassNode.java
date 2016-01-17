package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;
import java.util.Set;

public class ClassNode implements Node {
    public static ClassNodeBuilder builder(String name) {
        return new ClassNodeBuilder(name);
    }
    public static ClassNode declareClass(
            TypeName name,
            Set<TypeName> superTypes,
            List<FieldDeclarationNode> fields,
            ConstructorNode constructor,
            List<MethodNode> methods) {
        return new ClassNode(name, superTypes, fields, constructor, methods);
    }
    
    private final TypeName name;
    private final Set<TypeName> superTypes;
    private final List<FieldDeclarationNode> fields;
    private final ConstructorNode constructor;
    private final List<MethodNode> methods;
    
    private ClassNode(
            TypeName name,
            Set<TypeName> superTypes,
            List<FieldDeclarationNode> fields,
            ConstructorNode constructor,
            List<MethodNode> methodNodes) {
        this.name = name;
        this.superTypes = superTypes;
        this.fields = fields;
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
            transformer.visit(constructor),
            transformer.transformMethods(methods));
    }

    @Override
    public String toString() {
        return "ClassNode(" +
            "name=" + name +
            ", superTypes=" + superTypes +
            ", fields=" + fields +
            ", constructor=" + constructor +
            ", methods=" + methods +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassNode classNode = (ClassNode) o;

        if (name != null ? !name.equals(classNode.name) : classNode.name != null) return false;
        if (superTypes != null ? !superTypes.equals(classNode.superTypes) : classNode.superTypes != null) return false;
        if (fields != null ? !fields.equals(classNode.fields) : classNode.fields != null) return false;
        if (constructor != null ? !constructor.equals(classNode.constructor) : classNode.constructor != null)
            return false;
        return methods != null ? methods.equals(classNode.methods) : classNode.methods == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (superTypes != null ? superTypes.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        result = 31 * result + (constructor != null ? constructor.hashCode() : 0);
        result = 31 * result + (methods != null ? methods.hashCode() : 0);
        return result;
    }
}
