package org.zwobble.couscous.backends.csharp.primitives;

import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.backends.csharp.CsharpNodeTypes;
import org.zwobble.couscous.types.Type;

import java.util.List;

public class PrimitiveStaticMethodCall implements ExpressionNode {
    private final PrimitiveStaticMethod method;
    private final List<ExpressionNode> arguments;

    public PrimitiveStaticMethodCall(PrimitiveStaticMethod method, List<ExpressionNode> arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Type getType() {
        return method.getReturnType();
    }

    @Override
    public int type() {
        return CsharpNodeTypes.PRIMITIVE_STATIC_METHOD_CALL;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return arguments;
    }

    @Override
    public ExpressionNode transform(NodeTransformer transformer) {
        return new PrimitiveStaticMethodCall(
            method,
            transformer.transformExpressions(arguments)
        );
    }

    public Node generate() {
        return method.generate(arguments);
    }
}
