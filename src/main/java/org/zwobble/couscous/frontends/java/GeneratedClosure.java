package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.ReferenceNode;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.util.InsertionOrderSet;

import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.util.ExtraLists.concat;
import static org.zwobble.couscous.util.ExtraLists.list;

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

    public ConstructorCallNode generateConstructor() {
        return generateConstructor(list());
    }

    public ConstructorCallNode generateConstructor(Iterable<ExpressionNode> arguments) {
        return constructorCall(
            getType(),
            concat(captures, arguments));
    }

    private ScalarType getType() {
        return classNode.getName();
    }
}
