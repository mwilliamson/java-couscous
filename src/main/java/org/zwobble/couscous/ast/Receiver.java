package org.zwobble.couscous.ast;

import java.util.function.Function;

public interface Receiver extends Node {
    Receiver replaceExpressions(Function<ExpressionNode, ExpressionNode> replace);
    <T> T accept(Mapper<T> mapper);

    interface Mapper<T> {
        T visit(ExpressionNode receiver);
        T visit(TypeName receiver);
    }
}
