package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

public class FieldDeclarationNode implements Node {
    public static FieldDeclarationNode field(String name, TypeName type) {
        return new FieldDeclarationNode(name, type);
    }
    
    private final String name;
    private final TypeName type;
    
    private FieldDeclarationNode(String name, TypeName type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public TypeName getType() {
        return type;
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    public FieldDeclarationNode transform(NodeTransformer transformer) {
        return new FieldDeclarationNode(
            name,
            transformer.transform(type));
    }

    @Override
    public String toString() {
        return "FieldDeclarationNode(name=" + name + ", type=" + type + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        FieldDeclarationNode other = (FieldDeclarationNode) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
