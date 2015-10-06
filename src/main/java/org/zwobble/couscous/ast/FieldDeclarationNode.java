package org.zwobble.couscous.ast;

import lombok.Value;

@Value(staticConstructor="field")
public class FieldDeclarationNode {
    String name;
    TypeName type;
}
