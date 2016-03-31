package org.zwobble.couscous.backends;

import org.zwobble.couscous.ast.MethodSignature;

public interface Naming {
    static Naming signaturesContainsSimpleNames() {
        return new SignaturesContainFullyQualifiedNames();
    }

    String methodName(MethodSignature signature);
}
