package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ReferenceNode;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.util.InsertionOrderSet;

public class GeneratedClosure {
    private final ClassNode classNode;
    private final InsertionOrderSet<ReferenceNode> captures;

    public GeneratedClosure(ClassNode classNode, InsertionOrderSet<ReferenceNode> captures) {
        this.classNode = classNode;
        this.captures = captures;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public ScalarType getType() {
        return classNode.getName();
    }

    public InsertionOrderSet<ReferenceNode> getCaptures() {
        return captures;
    }
}
