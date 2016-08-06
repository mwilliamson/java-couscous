package org.zwobble.couscous.transforms;

import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.NodeTypes;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.sugar.SwitchCaseNode;
import org.zwobble.couscous.ast.sugar.SwitchNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.frontends.java.Scope;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.util.ExtraLists;

import java.util.List;

import static com.google.common.collect.Iterables.tryFind;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.TypeCoercionNode.coerce;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.sugar.SwitchCaseNode.switchCase;
import static org.zwobble.couscous.util.ExtraIterables.lazyFlatMap;
import static org.zwobble.couscous.util.ExtraLists.*;
import static org.zwobble.couscous.util.Fold.foldRight;
import static org.zwobble.couscous.util.Tails.tails;
import static org.zwobble.couscous.util.UpToAndIncludingIterable.upToAndIncluding;

public class DesugarSwitchToIfElse {
    // TODO: ensure globally unique IDs, and locally unique variable names

    public static NodeTransformer transformer() {
        Scope scope = Scope.create().temporaryPrefix("_couscous_desugar_switch_to_if");
        return DesugarStatement.<SwitchNode>transformer(NodeTypes.SWITCH, node -> desugar(scope, node));
    }

    public static List<StatementNode> desugar(Scope scope, SwitchNode switchNode) {
        LocalVariableDeclarationNode switchValueAssignment = scope.temporaryVariable(switchNode.getValue());

        List<StatementNode> handleDefault = tryFind(switchNode.getCases(), SwitchCaseNode::isDefault)
            .transform(SwitchCaseNode::getStatements)
            .or(list());

        List<SwitchCaseNode> cases = eagerMap(
            tails(switchNode.getCases()),
            remainingCases -> readSwitchCase(remainingCases.get(0), remainingCases)
        );

        return cons(
            switchValueAssignment,
            foldRight(cases, handleDefault, (handle, currentCase) ->
                currentCase.getValue()
                    .map(value -> list(ifStatement(
                        methodCall(
                            reference(switchValueAssignment),
                            "equals",
                            list(coerce(value, Types.OBJECT)),
                            Types.BOOLEAN
                        ),
                        currentCase.getStatements(),
                        handle
                    )))
                    .orElse(handle)
            )
        );
    }

    private static SwitchCaseNode readSwitchCase(SwitchCaseNode caseStatement, List<SwitchCaseNode> cases)
    {
        Iterable<StatementNode> statementsForCase = upToAndIncluding(
            lazyFlatMap(cases, SwitchCaseNode::getStatements),
            DesugarSwitchToIfElse::isEndOfCase
        );
        return switchCase(
            caseStatement.getValue(),
            ExtraLists.copyOf(statementsForCase)
        );
    }

    private static boolean isEndOfCase(StatementNode statement) {
        return statement.nodeType() == NodeTypes.RETURN;
    }
}
