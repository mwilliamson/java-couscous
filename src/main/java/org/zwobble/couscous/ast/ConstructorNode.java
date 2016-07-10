package org.zwobble.couscous.ast;

import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.Collections;
import java.util.List;

public class ConstructorNode implements Node {
    public static final ConstructorNode DEFAULT = constructor(Collections.emptyList(), Collections.emptyList());

    public static ConstructorNode constructor(
            List<FormalArgumentNode> arguments,
            List<StatementNode> body) {
        return new ConstructorNode(arguments, body);
    }
    
    private final List<FormalArgumentNode> arguments;
    private final List<StatementNode> body;
    
    public ConstructorNode(
            List<FormalArgumentNode> arguments,
            List<StatementNode> body) {
        this.arguments = arguments;
        this.body = body;
    }
    
    public List<FormalArgumentNode> getArguments() {
        return arguments;
    }
    
    public List<StatementNode> getBody() {
        return body;
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return Iterables.concat(arguments, body);
    }

    public ConstructorNode transform(NodeTransformer transformer) {
        return new ConstructorNode(
            transformer.transformFormalArguments(arguments),
            transformer.transformStatements(body));
    }

    @Override
    public String toString() {
        return "ConstructorNode(arguments=" + arguments + ", body=" + body
               + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result + ((body == null) ? 0 : body.hashCode());
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
        ConstructorNode other = (ConstructorNode) obj;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        return true;
    }
}
