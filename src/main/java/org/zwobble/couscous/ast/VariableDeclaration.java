package org.zwobble.couscous.ast;

import lombok.Value;

@Value(staticConstructor="var")
public class VariableDeclaration {
    int id;
    String name;
    TypeName type;
}
