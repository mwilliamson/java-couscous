package org.zwobble.couscous.ast.identifiers;

import static org.zwobble.couscous.util.ExtraLists.list;

public class TopIdentifier implements Identifier {
    static final Identifier INSTANCE = new TopIdentifier();

    private TopIdentifier() {
    }

    @Override
    public Iterable<IdentifierPart> getParts() {
        return list();
    }
}
