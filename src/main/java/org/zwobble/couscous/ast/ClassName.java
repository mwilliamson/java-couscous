package org.zwobble.couscous.ast;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class ClassName {
    public static ClassName of(String qualifiedName) {
        return new ClassName(
            qualifiedName,
            qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
    }
    
    String qualifiedName;
    String simpleName;
}
