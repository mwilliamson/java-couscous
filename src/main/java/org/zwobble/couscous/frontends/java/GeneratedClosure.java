package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.ReferenceNode;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.TypeParameter;
import org.zwobble.couscous.util.InsertionOrderSet;

import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.types.Types.addTypeParameters;
import static org.zwobble.couscous.util.ExtraLists.concat;
import static org.zwobble.couscous.util.ExtraLists.list;

public class GeneratedClosure {
    private final ClassNode classNode;
    private final InsertionOrderSet<TypeParameter> capturedTypes;
    private final InsertionOrderSet<ReferenceNode> capturedVariables;

    public GeneratedClosure(ClassNode classNode, InsertionOrderSet<TypeParameter> capturedTypes, InsertionOrderSet<ReferenceNode> capturedVariables) {
        this.classNode = classNode;
        this.capturedTypes = capturedTypes;
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
            addTypeParameters(getType(), ImmutableList.copyOf(capturedTypes.asList())),
            concat(capturedVariables, arguments));
    }

    private ScalarType getType() {
        return classNode.getName();
    }
}
