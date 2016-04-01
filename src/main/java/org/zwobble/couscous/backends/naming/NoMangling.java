package org.zwobble.couscous.backends.naming;

import org.zwobble.couscous.ast.MethodSignature;

public class NoMangling implements Naming {
    @Override
    public String methodName(MethodSignature signature) {
        return signature.getName();
    }
}
