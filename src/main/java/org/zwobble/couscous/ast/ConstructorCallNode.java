package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;

import java.util.List;

public class ConstructorCallNode implements ExpressionNode {
    public static ConstructorCallNode constructorCall(
        Type type,
        List<? extends ExpressionNode> arguments)
    {
        return new ConstructorCallNode(type, arguments);
    }
    
    private final Type type;
    private final List<? extends ExpressionNode> arguments;
    
    public ConstructorCallNode(
        Type type,
        List<? extends ExpressionNode> arguments)
    {
        this.type = type;
        this.arguments = arguments;
    }
    
    @Override
    public Type getType() {
        return type;
    }
    
    public List<? extends ExpressionNode> getArguments() {
        return arguments;
    }
    
    @Override
    public int nodeType() {
        return NodeTypes.CONSTRUCTOR_CALL;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return arguments;
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
        return new ConstructorCallNode(
            transformer.transform(type),
            transformer.transformExpressions(arguments));
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
