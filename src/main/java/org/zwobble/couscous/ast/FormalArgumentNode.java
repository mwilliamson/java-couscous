package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;

public class FormalArgumentNode implements VariableNode, Node {
    public static FormalArgumentNode formalArg(VariableDeclaration declaration) {
        return new FormalArgumentNode(declaration);
    }
    
    private final VariableDeclaration declaration;
    
    private FormalArgumentNode(VariableDeclaration declaration) {
        this.declaration = declaration;
    }
    
    public VariableDeclaration getDeclaration() {
        return declaration;
    }
    
    public TypeName getType() {
        return declaration.getType();
    }
    
    public String getName() {
        return declaration.getName();
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FormalArgumentNode(declaration=" + declaration + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((declaration == null) ? 0 : declaration.hashCode());
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
        FormalArgumentNode other = (FormalArgumentNode) obj;
        if (declaration == null) {
            if (other.declaration != null)
                return false;
        } else if (!declaration.equals(other.declaration))
            return false;
        return true;
    }
}
