package org.zwobble.couscous.ast;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Builder
@Value
public class MethodNode {
    public static MethodNodeBuilder staticMethod(String name) {
        return builder().isStatic(true).name(name);
    }
    
    boolean isStatic;
    String name;
    @Singular
    List<FormalArgumentNode> arguments; 
    @Singular("statement")
    List<StatementNode> body;
}
