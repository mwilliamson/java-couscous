package org.zwobble.couscous.ast;

import lombok.Value;

@Value
public class FormalArgumentNode implements VariableNode {
    int id;
    String name;
}
