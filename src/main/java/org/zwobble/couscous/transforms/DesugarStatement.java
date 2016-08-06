package org.zwobble.couscous.transforms;

import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class DesugarStatement {
    static <T extends StatementNode> NodeTransformer transformer(int nodeType, Function<T, List<StatementNode>> desugar) {
        return NodeTransformer.builder()
            .transformStatement(statement -> {
                if (statement.nodeType() == nodeType) {
                    return Optional.of(desugar.apply((T) statement));
                } else {
                    return Optional.empty();
                }
            })
            .build();
    }
}
