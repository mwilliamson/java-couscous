package org.zwobble.couscous.backends.naming;

import org.zwobble.couscous.ast.MethodSignature;

public interface Naming {
    static Naming signaturesContainSimpleNames() {
        return new SignaturesContainSimpleNames();
    }

    static Naming noMangling() {
        return new NoMangling();
    }

    String methodName(MethodSignature signature);
}
