package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.ReferenceNode;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.util.InsertionOrderSet;

import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.types.Types.addTypeParameters;
import static org.zwobble.couscous.util.ExtraLists.*;

public class GeneratedClosure {
    private final ClassNode classNode;
    private final InsertionOrderSet<ReferenceNode> capturedVariables;

    public GeneratedClosure(ClassNode classNode, InsertionOrderSet<ReferenceNode> capturedVariables) {
        this.classNode = classNode;
        this.capturedVariables = capturedVariables;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public ConstructorCallNode generateConstructor() {
        return generateConstructor(list());
    }

    public ConstructorCallNode generateConstructor(Iterable<ExpressionNode> arguments) {
        return constructorCall(
            // TODO: tidy up type parameters to avoid copy
            addTypeParameters(getType(), eagerMap(classNode.getTypeParameters(), parameter -> parameter.getType())),
            concat(capturedVariables, arguments));
    }

    public boolean hasCaptures() {
        return !capturedVariables.isEmpty();
    }

    private ScalarType getType() {
        return classNode.getName();
    }
}
