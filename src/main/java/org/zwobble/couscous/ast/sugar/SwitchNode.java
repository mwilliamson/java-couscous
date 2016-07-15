package org.zwobble.couscous.ast.sugar;

import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.NodeTypes;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;

import static org.zwobble.couscous.util.ExtraIterables.lazyCons;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class SwitchNode implements StatementNode {
    private final ExpressionNode value;
    private final List<SwitchCaseNode> cases;

    public SwitchNode(ExpressionNode value, List<SwitchCaseNode> cases) {
        this.value = value;
        this.cases = cases;
    }

    public ExpressionNode getValue() {
        return value;
    }

    public List<SwitchCaseNode> getCases() {
        return cases;
    }

    @Override
    public int type() {
        return NodeTypes.SWITCH;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return lazyCons(value, cases);
    }

    @Override
    public StatementNode transform(NodeTransformer transformer) {
        return new SwitchNode(
            transformer.transformExpression(value),
            eagerMap(cases, caseNode -> caseNode.transform(transformer))
        );
    }
}
