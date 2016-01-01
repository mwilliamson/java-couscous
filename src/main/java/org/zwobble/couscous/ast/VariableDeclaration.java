package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.identifiers.Identifier;

public class VariableDeclaration {
    public static VariableDeclaration var(Identifier id, String name, TypeName type) {
        return new VariableDeclaration(id, name, type);
    }
    
    private final Identifier id;
    private final String name;
    private final TypeName type;
    
    private VariableDeclaration(Identifier id, String name, TypeName type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
    
    public Identifier getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public TypeName getType() {
        return type;
    }

    @Override
    public String toString() {
        return "VariableDeclaration(id=" + id + ", name=" + name + ", type="
               + type + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        VariableDeclaration other = (VariableDeclaration) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
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
