package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

public class AnnotationNode implements Node {
    public static AnnotationNode annotation(Type type) {
        return new AnnotationNode(type);
    }
    
    private final Type type;
    
    private AnnotationNode(Type type) {
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }

    @Override
    public int nodeType() {
        return NodeTypes.ANNOTATION;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.empty();
    }

    public AnnotationNode transformSubtree(NodeTransformer transformer) {
        return new AnnotationNode(transformer.transform(type));
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
