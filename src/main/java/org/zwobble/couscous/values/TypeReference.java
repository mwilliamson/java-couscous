package org.zwobble.couscous.values;

import lombok.Value;

@Value(staticConstructor="typeRef")
public class TypeReference {
    String name;
}
