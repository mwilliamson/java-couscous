package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.identifiers.Identifiers;
import org.zwobble.couscous.types.*;
import org.zwobble.couscous.types.ParameterizedType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.TypeParameter;
import org.zwobble.couscous.util.Optionals;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.zwobble.couscous.types.ParameterizedType.parameterizedType;
import static org.zwobble.couscous.types.TypeParameter.typeParameter;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaTypes {
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
            return new AnonymousType(typeBinding.getKey());
        }
        if (typeBinding.isArray()) {
            return Types.array(typeOf(typeBinding.getElementType()));
        }
        ITypeBinding outerClass = typeBinding.getDeclaringClass();
        if (typeBinding.isTypeVariable()) {
            Identifier declaringScope = getDeclaringScope(typeBinding);
            return new TypeParameter(declaringScope, typeBinding.getName());
        }
        ScalarType rawType = outerClass == null
            ? ScalarType.of(typeBinding.getErasure().getQualifiedName())
            // TODO: test for erasure of inner type name
            : ScalarType.of(erasure(typeOf(outerClass)).getQualifiedName() + "__" + typeBinding.getErasure().getName());
        if (typeBinding.isParameterizedType()) {
            List<Type> typeParameters = eagerMap(
                asList(typeBinding.getTypeArguments()),
                JavaTypes::typeOf);
            return new ParameterizedType(rawType, typeParameters);
        } else {
            return rawType;
        }
    }

    private static Identifier getDeclaringScope(ITypeBinding typeParameter) {
        if (typeParameter.getDeclaringClass() == null) {
            IMethodBinding method = typeParameter.getDeclaringMethod();
            // TODO: disambiguate overloads
            return Identifiers.method(identifierForType(method.getDeclaringClass()), method.getName());
        } else {
            return identifierForType(typeParameter.getDeclaringClass());
        }
    }

    private static Identifier identifierForType(ITypeBinding type) {
        return Identifiers.forType(erasure(typeOf(type)).getQualifiedName());
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

    public static Type bind(Type genericType, Type concreteType) {
        if (genericType.equals(concreteType)) {
            return concreteType;
        }
        return Optionals.flatMap(
            tryCast(ParameterizedType.class, genericType),
            tryCast(ParameterizedType.class, concreteType),
            JavaTypes::bindParameterizedType)
            .orElseGet(() -> {
                if (!(genericType instanceof TypeParameter)) {
                    return concreteType;
                } else {
                    return new BoundTypeParameter((TypeParameter) genericType, concreteType);
                }
            });
    }

    private static Optional<Type> bindParameterizedType(
        ParameterizedType genericType,
        ParameterizedType concreteType)
    {
        if (genericType.getRawType().equals(concreteType.getRawType())) {
            return Optional.of(new ParameterizedType(
                genericType.getRawType(),
                eagerMap(
                    genericType.getParameters(),
                    concreteType.getParameters(),
                    JavaTypes::bind)));
        } else {
            return Optional.empty();
        }
    }

    public static Type iterable(Type elementType) {
        return parameterizedType(ScalarType.of("java.lang.Iterable"), list(elementType));
    }

    private static final ScalarType RAW_ITERATOR = ScalarType.of("java.util.Iterator");
    public static TypeParameter ITERATOR_TYPE_PARAMETER = typeParameter(Identifiers.type(Identifiers.TOP, RAW_ITERATOR.getQualifiedName()), "T");

    public static Type iterator(Type elementType) {
        return parameterizedType(RAW_ITERATOR, list(elementType));
    }
}
