package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.VariableDeclaration;

import java.util.List;

public class GeneratedClosure {
    private final ClassNode classNode;
    private final List<VariableDeclaration> captures;

    public GeneratedClosure(ClassNode classNode, List<VariableDeclaration> captures) {
        this.classNode = classNode;
        this.captures = captures;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public TypeName getType() {
        return classNode.getName();
    }

    public List<VariableDeclaration> getCaptures() {
        return captures;
    }
}
