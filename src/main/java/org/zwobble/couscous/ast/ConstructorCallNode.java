package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

public class ConstructorCallNode implements ExpressionNode {
    public static ConstructorCallNode constructorCall(
            TypeName type,
            List<ExpressionNode> arguments) {
        return new ConstructorCallNode(type, arguments);
    }
    
    private final TypeName type;
    private final List<ExpressionNode> arguments;
    
    public ConstructorCallNode(
            TypeName type,
            List<ExpressionNode> arguments) {
        this.type = type;
        this.arguments = arguments;
    }
    
    @Override
    public TypeName getType() {
        return type;
    }
    
    public List<ExpressionNode> getArguments() {
        return arguments;
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ConstructorCallNode(type=" + type + ", arguments=" + arguments
               + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((arguments == null) ? 0 : arguments.hashCode());
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
        ConstructorCallNode other = (ConstructorCallNode) obj;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
