package org.zwobble.couscous.backends.csharp.primitives;

import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.backends.csharp.CsharpNodeTypes;
import org.zwobble.couscous.types.Type;

import java.util.List;

import static org.zwobble.couscous.util.ExtraIterables.lazyCons;

public class PrimitiveInstanceMethodCall implements ExpressionNode {
    private final PrimitiveInstanceMethod method;
    private final ExpressionNode receiver;
    private final List<ExpressionNode> arguments;

    public PrimitiveInstanceMethodCall(PrimitiveInstanceMethod method, ExpressionNode receiver, List<ExpressionNode> arguments) {
        this.method = method;
        this.receiver = receiver;
        this.arguments = arguments;
    }

    @Override
    public Type getType() {
        return method.getReturnType();
    }

    @Override
    public int type() {
        return CsharpNodeTypes.PRIMITIVE_INSTANCE_METHOD_CALL;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return lazyCons(receiver, arguments);
    }

    @Override
    public ExpressionNode transform(NodeTransformer transformer) {
        return new PrimitiveInstanceMethodCall(
            method,
            transformer.transformExpression(receiver),
            transformer.transformExpressions(arguments)
        );
    }

    public Node generate() {
        return method.generate(receiver, arguments);
    }
}
