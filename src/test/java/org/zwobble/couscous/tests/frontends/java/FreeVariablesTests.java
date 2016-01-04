package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;
import org.zwobble.couscous.ast.ReferenceNode;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.tests.TestIds;
import org.zwobble.couscous.values.StringValue;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.frontends.java.FreeVariables.findFreeVariables;

public class FreeVariablesTests {
    @Test
    public void referenceIsFreeIfThereIsNoAssociatedDeclaration() {
        VariableDeclaration declaration = var(TestIds.ANY_ID, "[name]", StringValue.REF);
        List<ReferenceNode> freeVariables = findFreeVariables(asList(reference(declaration)));
        assertEquals(asList(reference(declaration)), freeVariables);
    }

    @Test
    public void referenceIsNotFreeIfIsDeclaredAsArgument() {
        VariableDeclaration declaration = var(TestIds.ANY_ID, "[name]", StringValue.REF);
        List<ReferenceNode> freeVariables = findFreeVariables(
            asList(formalArg(declaration), reference(declaration)));
        assertEquals(emptyList(), freeVariables);
    }

    @Test
    public void referenceIsNotFreeIfIsReferenceToLocalDefinedInBody() {
        VariableDeclaration declaration = var(TestIds.ANY_ID, "[name]", StringValue.REF);
        List<ReferenceNode> freeVariables = findFreeVariables(
            asList(
                localVariableDeclaration(declaration, literal("[value]")),
                reference(declaration)));
        assertEquals(emptyList(), freeVariables);
    }
}
