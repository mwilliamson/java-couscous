package org.zwobble.couscous.ast;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class TypeName {
    public static TypeName of(String qualifiedName) {
        return new TypeName(
            qualifiedName,
            qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
    }
    
    String qualifiedName;
    String simpleName;
}
