package org.zwobble.couscous.ast;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class TypeName {
    public static TypeName of(String qualifiedName) {
        return new TypeName(qualifiedName);
    }
    
    String qualifiedName;
    
    public String getSimpleName() {
        return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
    }
}
