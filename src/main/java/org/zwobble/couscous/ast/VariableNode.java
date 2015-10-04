package org.zwobble.couscous.ast;

import org.zwobble.couscous.values.TypeReference;

public interface VariableNode {
    int getId();
    TypeReference getType();
}
