package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.ast.types.Type;
import org.zwobble.couscous.ast.types.Types;
import org.zwobble.couscous.values.ObjectValues;

import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

class JavaTypes {
    static Type typeOf(Expression expression) {
        return typeOf(expression.resolveTypeBinding());
    }

    static Type typeOf(IVariableBinding variableBinding) {
        return typeOf(variableBinding.getType());
    }

    static Type typeOf(org.eclipse.jdt.core.dom.Type type) {
        return typeOf(type.resolveBinding());
    }

    static Type typeOf(ITypeBinding typeBinding) {
        ITypeBinding outerClass = typeBinding.getDeclaringClass();
        if (outerClass == null) {
            return ScalarType.of(typeBinding.getErasure().getQualifiedName());
        } else {
            return ScalarType.of(Types.erasure(typeOf(outerClass)).getQualifiedName() + "__" + typeBinding.getName());
        }
    }

    static Set<Type> superTypes(TypeDeclaration declaration) {
        return superTypes(declaration.resolveBinding());
    }

    static Set<Type> superTypes(AnonymousClassDeclaration declaration) {
        return superTypes(declaration.resolveBinding());
    }

    static Set<Type> superTypesAndSelf(ITypeBinding typeBinding) {
        ImmutableSet.Builder<Type> superTypes = ImmutableSet.builder();
        superTypes.addAll(superTypes(typeBinding));
        superTypes.add(typeOf(typeBinding));
        return superTypes.build();
    }

    private static Set<Type> superTypes(ITypeBinding typeBinding) {
        ImmutableSet.Builder<Type> superTypes = ImmutableSet.builder();
        superClass(typeBinding).ifPresent(superTypes::add);
        superTypes.addAll(transform(
            asList(typeBinding.getInterfaces()),
            JavaTypes::typeOf));
        return superTypes.build();
    }

    private static Optional<Type> superClass(ITypeBinding typeBinding) {
        return Optional.ofNullable(typeBinding.getSuperclass())
            .map(JavaTypes::typeOf)
            .filter(type -> !type.equals(ObjectValues.OBJECT));
    }
}
