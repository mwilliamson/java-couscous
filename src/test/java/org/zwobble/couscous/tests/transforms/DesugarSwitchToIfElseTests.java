package org.zwobble.couscous.tests.transforms;

import org.junit.Test;
import org.zwobble.couscous.ast.Operations;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.ast.identifiers.Identifiers;
import org.zwobble.couscous.ast.sugar.SwitchNode;
import org.zwobble.couscous.frontends.java.Scope;
import org.zwobble.couscous.transforms.DesugarSwitchToIfElse;
import org.zwobble.couscous.types.Types;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.sugar.SwitchCaseNode.switchCase;
import static org.zwobble.couscous.util.ExtraLists.list;

public class DesugarSwitchToIfElseTests {
    @Test
    public void switchStatementWithoutFallThroughIsReadAsIfElseStatement() {
        VariableDeclaration tmp = var(
            Identifiers.variable(Identifiers.TOP, "_couscous_tmp_0"),
            "_couscous_tmp_0",
            Types.STRING
        );
        assertEquals(
            list(
                localVariableDeclaration(tmp, literal("one")),
                ifStatement(
                    Operations.methodEquals(reference(tmp), literal("one")),
                    list(returns(literal(1))),
                    list(
                        ifStatement(
                            Operations.methodEquals(reference(tmp), literal("two")),
                            list(returns(literal(2))),
                            list(
                                returns(literal(0))
                            )
                        )
                    )
                )
            ),
            DesugarSwitchToIfElse.desugar(
                Scope.create(),
                new SwitchNode(
                    literal("one"),
                    list(
                        switchCase(literal("one"), list(returns(literal(1)))),
                        switchCase(literal("two"), list(returns(literal(2)))),
                        switchCase(list(returns(literal(0))))
                    )
                )
            )
        );
    }

    @Test
    public void switchStatementWithoutDefaultIsReadAsIfStatementWithoutElseStatement() {
        VariableDeclaration tmp = var(
            Identifiers.variable(Identifiers.TOP, "_couscous_tmp_0"),
            "_couscous_tmp_0",
            Types.STRING
        );
        assertEquals(
            list(
                localVariableDeclaration(tmp, literal("one")),
                ifStatement(
                    Operations.methodEquals(reference(tmp), literal("two")),
                    list(returns(literal(0))),
                    list()
                )
            ),
            DesugarSwitchToIfElse.desugar(
                Scope.create(),
                new SwitchNode(
                    literal("one"),
                    list(
                        switchCase(literal("two"), list(returns(literal(0))))
                    )
                )
            )
        );
    }

    @Test
    public void casesCanFallthrough() {
        VariableDeclaration tmp = var(
            Identifiers.variable(Identifiers.TOP, "_couscous_tmp_0"),
            "_couscous_tmp_0",
            Types.STRING
        );
        assertEquals(
            list(
                localVariableDeclaration(tmp, literal("one")),
                ifStatement(
                    Operations.methodEquals(reference(tmp), literal("one")),
                    list(
                        expressionStatement(literal(1)),
                        returns(literal(2))
                    ),
                    list(
                        ifStatement(
                            Operations.methodEquals(reference(tmp), literal("two")),
                            list(
                                returns(literal(2))
                            ),
                            list()
                        )
                    )
                )
            ),
            DesugarSwitchToIfElse.desugar(
                Scope.create(),
                new SwitchNode(
                    literal("one"),
                    list(
                        switchCase(literal("one"), list(expressionStatement(literal(1)))),
                        switchCase(literal("two"), list(returns(literal(2))))
                    )
                )
            )
        );
    }
}
