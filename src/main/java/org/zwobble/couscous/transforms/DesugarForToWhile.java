package org.zwobble.couscous.transforms;

import org.zwobble.couscous.ast.ForNode;
import org.zwobble.couscous.ast.NodeTypes;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.frontends.java.Scope;

import java.util.List;
import java.util.Optional;

import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.util.ExtraIterables.lazyMap;
import static org.zwobble.couscous.util.ExtraLists.append;
import static org.zwobble.couscous.util.ExtraLists.concat;

public class DesugarForToWhile {
    public static NodeTransformer transformer() {
        Scope scope = Scope.create().temporaryPrefix("_couscous_desugar_for_to_while");
        return NodeTransformer.builder()
            .transformStatement(statement -> {
                if (statement.type() == NodeTypes.FOR) {
                    return Optional.of(desugar(scope, ((ForNode) statement)));
                } else {
                    return Optional.empty();
                }
            })
            .build();
    }

    public static List<StatementNode> desugar(Scope scope, ForNode forNode) {
        return append(
            forNode.getInitializers(),
            whileLoop(
                forNode.getCondition(),
                concat(
                    forNode.getStatements(),
                    lazyMap(forNode.getUpdaters(), updater -> expressionStatement(updater))
                )
            )
        );
    }
}
