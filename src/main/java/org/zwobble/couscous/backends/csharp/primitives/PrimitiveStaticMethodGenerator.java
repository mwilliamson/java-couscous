package org.zwobble.couscous.backends.csharp.primitives;

import org.zwobble.couscous.ast.ExpressionNode;

import java.util.List;

@FunctionalInterface
interface PrimitiveStaticMethodGenerator {
    ExpressionNode generate(List<ExpressionNode> arguments);
}
