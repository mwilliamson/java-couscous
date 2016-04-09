package org.zwobble.couscous.ast.identifiers;

public class Identifiers {
    public static final Identifier TOP = new TopIdentifier();

    public static Identifier type(Identifier identifier, String name) {
        return extend(identifier, name, IdentifierType.TYPE);
    }

    public static Identifier method(Identifier identifier, String name) {
        return extend(identifier, name, IdentifierType.METHOD);
    }

    public static Identifier constructor(Identifier identifier) {
        return extend(identifier, "", IdentifierType.CONSTRUCTOR);
    }

    public static Identifier variable(Identifier identifier, String name) {
        return extend(identifier, name, IdentifierType.VARIABLE);
    }

    private static Identifier extend(Identifier identifier, String name, IdentifierType type) {
        return new ExtendedIdentifier(identifier, name, type);
    }

    public static Identifier forType(String name) {
        return type(TOP, name);
    }
}
