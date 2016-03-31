package org.zwobble.couscous.backends;

import org.zwobble.couscous.ast.MethodSignature;

public class SignaturesContainFullyQualifiedNames implements Naming {
    @Override
    public String methodName(MethodSignature signature) {
        return Names.toUniqueName(signature);
    }
}
