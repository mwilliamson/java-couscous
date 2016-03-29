package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.types.ParameterizedType;
import org.zwobble.couscous.types.*;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.TypeParameter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

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
        if (typeBinding.isAnonymous()) {
            throw new RuntimeException("Cannot get type of anonymous type binding");
        }
        ITypeBinding outerClass = typeBinding.getDeclaringClass();
        if (outerClass == null) {
            ScalarType rawType = ScalarType.of(typeBinding.getErasure().getQualifiedName());
            if (typeBinding.isParameterizedType()) {
                List<Type> typeParameters = eagerMap(
                    asList(typeBinding.getTypeArguments()),
                    JavaTypes::typeOf);
                return new ParameterizedType(rawType, typeParameters);
            } else {
                return rawType;
            }
        } else if (typeBinding.isTypeVariable()) {
            return new TypeParameter(erasure(typeOf(outerClass)), typeBinding.getName());
        } else {
            return ScalarType.of(erasure(typeOf(outerClass)).getQualifiedName() + "__" + typeBinding.getName());
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
            .filter(type -> !type.equals(Types.OBJECT));
    }

    static Type bind(Type genericType, Type concreteType) {
        if (genericType.equals(concreteType)) {
            return concreteType;
        } else if (!(genericType instanceof TypeParameter)) {
            throw new RuntimeException("Type parameter was " + genericType);
        } else {
            return new BoundTypeParameter((TypeParameter) genericType, concreteType);
        }
    }
}
