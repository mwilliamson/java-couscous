package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.NodeVisitor;

public class ClassNode implements Node {
    public static ClassNodeBuilder builder(String name) {
        return new ClassNodeBuilder(name);
    }
    
    public static ClassNode declareClass(
            TypeName name,
            List<FieldDeclarationNode> fields,
            ConstructorNode constructor,
            List<MethodNode> methods) {
        return new ClassNode(name, fields, constructor, methods);
    }
    
    private final TypeName name;
    private final List<FieldDeclarationNode> fields;
    private final ConstructorNode constructor;
    private final List<MethodNode> methods;
    
    private ClassNode(
            TypeName name,
            List<FieldDeclarationNode> fields,
            ConstructorNode constructor,
            List<MethodNode> methodNodes) {
        this.name = name;
        this.fields = fields;
        this.constructor = constructor;
        methods = methodNodes;
    }
    
    public TypeName getName() {
        return name;
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
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ClassNode(name=" + name + ", fields=" + fields
               + ", constructor=" + constructor + ", methods=" + methods + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((constructor == null) ? 0 : constructor.hashCode());
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
        result = prime * result + ((methods == null) ? 0 : methods.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassNode other = (ClassNode) obj;
        if (constructor == null) {
            if (other.constructor != null)
                return false;
        } else if (!constructor.equals(other.constructor))
            return false;
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        if (methods == null) {
            if (other.methods != null)
                return false;
        } else if (!methods.equals(other.methods))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
