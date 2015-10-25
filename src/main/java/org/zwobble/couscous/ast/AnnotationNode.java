package org.zwobble.couscous.ast;

public class AnnotationNode {
    public static AnnotationNode annotation(TypeName type) {
        return new AnnotationNode(type);
    }
    
    private final TypeName type;
    
    private AnnotationNode(TypeName type) {
        this.type = type;
    }
    
    public TypeName getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AnnotationNode(type=" + type + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        AnnotationNode other = (AnnotationNode) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}