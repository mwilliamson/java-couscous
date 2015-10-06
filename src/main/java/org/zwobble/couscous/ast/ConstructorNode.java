package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.NodeVisitor;

import lombok.Value;

@Value(staticConstructor="constructor")
public class ConstructorNode implements CallableNode {
    List<FormalArgumentNode> arguments;
    List<StatementNode> body;
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
