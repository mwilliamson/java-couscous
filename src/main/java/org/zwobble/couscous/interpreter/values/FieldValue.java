package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.TypeName;

import lombok.Value;

@Value
public class FieldValue {
    String name;
    TypeName type;
}
