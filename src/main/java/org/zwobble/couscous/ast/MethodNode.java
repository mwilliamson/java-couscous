package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.NodeVisitor;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Builder
@Value
public class MethodNode implements Node {
    public static MethodNodeBuilder staticMethod(String name) {
        return builder().isStatic(true).name(name);
    }
    
    public static MethodNodeBuilder method(String name) {
        return builder().isStatic(false).name(name);
    }
    
    boolean isStatic;
    String name;
    @Singular
    List<FormalArgumentNode> arguments; 
    @Singular("statement")
    List<StatementNode> body;
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
