package org.zwobble.couscous.ast.identifiers;

public class IdentifierPart {
    private final String name;
    private final IdentifierType type;

    public IdentifierPart(String name, IdentifierType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public IdentifierType getType() {
        return type;
    }
}
