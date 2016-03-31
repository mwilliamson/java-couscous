package org.zwobble.couscous.backends;

import org.zwobble.couscous.ast.MethodSignature;

public interface Naming {
    static Naming signaturesContainSimpleNames() {
        return new SignaturesContainSimpleNames();
    }

    String methodName(MethodSignature signature);
}
