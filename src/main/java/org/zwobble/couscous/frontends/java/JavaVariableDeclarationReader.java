package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.zwobble.couscous.ast.VariableDeclaration;

import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;

class JavaVariableDeclarationReader {
    static VariableDeclaration read(Scope scope, SingleVariableDeclaration parameter) {
        return scope.generateVariable(
            parameter.resolveBinding().getKey(),
            parameter.getName().getIdentifier(),
            typeOf(parameter.resolveBinding()));
    }
}
