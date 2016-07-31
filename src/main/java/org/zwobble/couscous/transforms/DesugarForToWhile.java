package org.zwobble.couscous.transforms;

import org.zwobble.couscous.ast.ForNode;
import org.zwobble.couscous.ast.NodeTypes;
import org.zwobble.couscous.ast.StatementBlockNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;

import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.util.ExtraIterables.lazyMap;
import static org.zwobble.couscous.util.ExtraLists.*;

public class DesugarForToWhile {
    public static NodeTransformer transformer() {
        return DesugarStatement.transformer(NodeTypes.FOR, DesugarForToWhile::desugar);
    }

    public static List<StatementNode> desugar(ForNode forNode) {
        List<StatementNode> statements = append(
            forNode.getInitializers(),
            whileLoop(
                forNode.getCondition(),
                concat(
                    forNode.getStatements(),
                    lazyMap(forNode.getUpdaters(), updater -> expressionStatement(updater))
                )
            )
        );
        return list(new StatementBlockNode(statements));
    }
}
