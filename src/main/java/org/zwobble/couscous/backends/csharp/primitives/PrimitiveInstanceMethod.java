package org.zwobble.couscous.backends.csharp.primitives;

import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.types.Type;

import java.util.List;

class PrimitiveInstanceMethod {
    private final PrimitiveInstanceMethodGenerator generate;
    private final Type returnType;

    PrimitiveInstanceMethod(PrimitiveInstanceMethodGenerator generate, Type returnType) {
        this.generate = generate;
        this.returnType = returnType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Node generate(ExpressionNode receiver, List<ExpressionNode> arguments) {
        return generate.generate(receiver, arguments);
    }
}
