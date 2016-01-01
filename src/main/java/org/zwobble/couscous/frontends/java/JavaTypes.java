package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.values.ObjectValues;

import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

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

    static Set<TypeName> superTypes(TypeDeclaration declaration) {
        return superTypes(declaration.resolveBinding());
    }

    static Set<TypeName> superTypes(AnonymousClassDeclaration declaration) {
        return superTypes(declaration.resolveBinding());
    }

    static Set<TypeName> superTypes(LambdaExpression expression) {
        return superTypesAndSelf(expression.resolveTypeBinding());
    }

    static Set<TypeName> superTypes(ExpressionMethodReference expression) {
        return superTypesAndSelf(expression.resolveTypeBinding());
    }

    static Set<TypeName> superTypesAndSelf(ITypeBinding typeBinding) {
        ImmutableSet.Builder<TypeName> superTypes = ImmutableSet.builder();
        superTypes.addAll(superTypes(typeBinding));
        superTypes.add(typeOf(typeBinding));
        return superTypes.build();
    }

    private static Set<TypeName> superTypes(ITypeBinding typeBinding) {
        ImmutableSet.Builder<TypeName> superTypes = ImmutableSet.builder();
        superTypes.addAll(transform(
            asList(typeBinding.getInterfaces()),
            JavaTypes::typeOf));
        superTypes.add(superClass(typeBinding));
        return superTypes.build();
    }

    private static TypeName superClass(ITypeBinding typeBinding) {
        if (typeBinding.getSuperclass() == null) {
            return ObjectValues.OBJECT;
        } else {
            return typeOf(typeBinding.getSuperclass());
        }
    }
}
