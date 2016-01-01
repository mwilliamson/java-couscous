package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.zwobble.couscous.ast.FormalArgumentNode;

import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;

class JavaVariableDeclarationReader {
    static FormalArgumentNode read(Scope scope, SingleVariableDeclaration parameter) {
        return scope.formalArgument(
            parameter.resolveBinding().getKey(),
            parameter.getName().getIdentifier(),
            typeOf(parameter.resolveBinding()));
    }
}
