package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Type;
import org.zwobble.couscous.ast.TypeName;

class JavaTypes {
    static TypeName typeOf(Expression expression) {
        return typeOf(expression.resolveTypeBinding());
    }

    static TypeName typeOf(IVariableBinding variableBinding) {
        return typeOf(variableBinding.getType());
    }

    static TypeName typeOf(Type type) {
        return typeOf(type.resolveBinding());
    }

    static TypeName typeOf(ITypeBinding typeBinding) {
        return TypeName.of(typeBinding.getQualifiedName());
    }
}
