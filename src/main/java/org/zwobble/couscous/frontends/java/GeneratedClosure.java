package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ReferenceNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.VariableDeclaration;

import java.util.List;

public class GeneratedClosure {
    private final ClassNode classNode;
    private final List<ReferenceNode> captures;

    public GeneratedClosure(ClassNode classNode, List<ReferenceNode> captures) {
        this.classNode = classNode;
        this.captures = captures;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public TypeName getType() {
        return classNode.getName();
    }

    public List<ReferenceNode> getCaptures() {
        return captures;
    }
}
