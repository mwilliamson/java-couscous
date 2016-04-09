package org.zwobble.couscous.tests;

import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.identifiers.Identifiers;

public class TestIds {
    public static final Identifier ANY_ID = Identifiers.variable(Identifiers.TOP, "[any id]");

    public static Identifier variable(String name) {
        return Identifiers.variable(ANY_ID, name);
    }
}
