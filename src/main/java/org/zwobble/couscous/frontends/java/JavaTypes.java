package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.values.ObjectValues;

import java.util.Optional;
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
        ITypeBinding outerClass = typeBinding.getDeclaringClass();
        if (outerClass == null) {
            return TypeName.of(typeBinding.getErasure().getQualifiedName());
        } else {
            return TypeName.of(typeOf(outerClass).getQualifiedName() + "__" + typeBinding.getName());
        }
    }

    static Set<TypeName> superTypes(TypeDeclaration declaration) {
        return superTypes(declaration.resolveBinding());
    }

    static Set<TypeName> superTypes(AnonymousClassDeclaration declaration) {
        return superTypes(declaration.resolveBinding());
    }

    static Set<TypeName> superTypesAndSelf(ITypeBinding typeBinding) {
        ImmutableSet.Builder<TypeName> superTypes = ImmutableSet.builder();
        superTypes.addAll(superTypes(typeBinding));
        superTypes.add(typeOf(typeBinding));
        return superTypes.build();
    }

    private static Set<TypeName> superTypes(ITypeBinding typeBinding) {
        ImmutableSet.Builder<TypeName> superTypes = ImmutableSet.builder();
        superClass(typeBinding).ifPresent(superTypes::add);
        superTypes.addAll(transform(
            asList(typeBinding.getInterfaces()),
            JavaTypes::typeOf));
        return superTypes.build();
    }

    private static Optional<TypeName> superClass(ITypeBinding typeBinding) {
        return Optional.ofNullable(typeBinding.getSuperclass())
            .map(JavaTypes::typeOf)
            .filter(type -> !type.equals(ObjectValues.OBJECT));
    }
}
