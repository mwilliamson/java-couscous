package org.zwobble.couscous.backends.naming;

import org.zwobble.couscous.ast.MethodSignature;

public class SignaturesContainSimpleNames implements Naming {
    @Override
    public String methodName(MethodSignature signature) {
        return Names.toUniqueName(signature);
    }
}
