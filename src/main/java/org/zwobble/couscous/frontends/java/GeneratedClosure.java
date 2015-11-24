package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.VariableDeclaration;

import java.util.List;

public class GeneratedClosure {
    private final TypeName type;
    private final List<VariableDeclaration> captures;

    public GeneratedClosure(TypeName type, List<VariableDeclaration> captures) {
        this.type = type;
        this.captures = captures;
    }

    public TypeName getType() {
        return type;
    }

    public List<VariableDeclaration> getCaptures() {
        return captures;
    }
}
