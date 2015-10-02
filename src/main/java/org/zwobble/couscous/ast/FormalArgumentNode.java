package org.zwobble.couscous.ast;

import org.zwobble.couscous.values.TypeReference;

import lombok.Value;

@Value
public class FormalArgumentNode implements VariableNode {
    int id;
    TypeReference type;
    String name;
}
