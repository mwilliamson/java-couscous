package org.zwobble.couscous.frontends.java;

import java.util.HashMap;
import java.util.Map;

public class UniqueIdentifiers {
    private final Map<String, Integer> identifiers;

    public UniqueIdentifiers() {
        identifiers = new HashMap<>();
    }

    public String generate(String identifier) {
        int index = identifiers.computeIfAbsent(identifier, x -> 0);
        return identifier + "__" + index;
    }
}
