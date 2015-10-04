package org.zwobble.couscous.ast;

import org.zwobble.couscous.values.TypeReference;

import lombok.Value;

@Value(staticConstructor="var")
public class VariableDeclaration {
    int id;
    String name;
    TypeReference type;
}
