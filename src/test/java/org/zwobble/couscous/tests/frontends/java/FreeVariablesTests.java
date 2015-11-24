package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.values.StringValue;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.frontends.java.FreeVariables.findFreeVariables;

public class FreeVariablesTests {
    @Test
    public void referenceIsFreeIfThereIsNoAssociatedDeclaration() {
        VariableDeclaration declaration = var("[id]", "[name]", StringValue.REF);
        List<VariableDeclaration> freeVariables = findFreeVariables(emptyList(), asList(reference(declaration)));
        assertEquals(asList(declaration), freeVariables);
    }

    @Test
    public void referenceIsNotFreeIfIsDeclaredAsArgument() {
        VariableDeclaration declaration = var("[id]", "[name]", StringValue.REF);
        List<VariableDeclaration> freeVariables = findFreeVariables(
            asList(formalArg(declaration)),
            asList(reference(declaration)));
        assertEquals(emptyList(), freeVariables);
    }
}
