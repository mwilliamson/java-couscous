package org.zwobble.couscous.backends.csharp.primitives;

import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.types.Type;

import java.util.List;

class PrimitiveStaticMethod {
    private final PrimitiveStaticMethodGenerator generate;
    private final Type returnType;

    PrimitiveStaticMethod(PrimitiveStaticMethodGenerator generate, Type returnType) {
        this.generate = generate;
        this.returnType = returnType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Node generate(List<ExpressionNode> arguments) {
        return generate.generate(arguments);
    }
}
