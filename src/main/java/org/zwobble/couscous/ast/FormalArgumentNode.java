package org.zwobble.couscous.ast;

import lombok.Value;

@Value(staticConstructor="formalArg")
public class FormalArgumentNode implements VariableNode {
    VariableDeclaration declaration;
    
    public TypeName getType() {
        return declaration.getType();
    }
    
    public String getName() {
        return declaration.getName();
    }
}
