package org.zwobble.couscous.tests;

import org.zwobble.couscous.ast.identifiers.Identifier;

public class TestIds {
    public static final Identifier ANY_ID = Identifier.TOP.variable("[any id]");

    public static Identifier variable(String name) {
        return ANY_ID.variable(name);
    }
}
