package org.zwobble.couscous.tests;

import org.zwobble.couscous.ast.identifiers.Identifier;

public class TestIds {
    public static final Identifier ANY_ID = Identifier.TOP.extend("[any id]");

    public static Identifier id(String name) {
        return ANY_ID.extend(name);
    }
}
