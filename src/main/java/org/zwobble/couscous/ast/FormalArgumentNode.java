package org.zwobble.couscous.ast;

import org.zwobble.couscous.values.TypeReference;

import lombok.Value;

@Value(staticConstructor="formalArg")
public class FormalArgumentNode implements VariableNode {
    VariableDeclaration declaration;
    
    public TypeReference getType() {
        return declaration.getType();
    }
}
