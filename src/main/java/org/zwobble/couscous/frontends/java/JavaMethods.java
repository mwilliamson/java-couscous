package org.zwobble.couscous.frontends.java;

import com.google.common.collect.Iterables;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.TypeParameter;

import java.util.List;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

class JavaMethods {
    static MethodSignature signature(IMethodBinding method) {
        ITypeBinding declaringClass = method.getDeclaringClass();
        ITypeBinding erasedDeclaringClass = declaringClass.getErasure();
        IMethodBinding declaringMethod = method.getMethodDeclaration();

        MethodSignature unboundSignature = unboundSignature(method);

        if (!declaringMethod.isEqualTo(method)) {
            return bind(unboundSignature(declaringMethod), unboundSignature);
        } else if (!declaringClass.isEqualTo(erasedDeclaringClass)) {
            IMethodBinding[] erasedMethods = erasedDeclaringClass.getDeclaredMethods();
            IMethodBinding erasedMethod = Iterables.getOnlyElement(Iterables.filter(
                asList(erasedMethods),
                m -> m.isSubsignature(method)));
            return bind(unboundSignature(erasedMethod), unboundSignature);
        } else {
            return unboundSignature;
        }
    }

    private static MethodSignature bind(MethodSignature genericSignature, MethodSignature concreteSignature) {
        if (!genericSignature.getTypeParameters().isEmpty()) {
            throw new UnsupportedOperationException();
        }
        return new MethodSignature(
            concreteSignature.getName(),
            list(),
            eagerMap(
                genericSignature.getArguments(),
                concreteSignature.getArguments(),
                JavaTypes::bind),
            JavaTypes.bind(genericSignature.getReturnType(), concreteSignature.getReturnType()));
    }

    private static MethodSignature unboundSignature(IMethodBinding method) {
        List<TypeParameter> typeParameters = eagerMap(
            asList(method.getTypeParameters()),
            parameter -> new TypeParameter(identifierFor(method), parameter.getName()));
        List<Type> argumentTypes = eagerMap(asList(method.getParameterTypes()), JavaTypes::typeOf);
        Type returnType = typeOf(method.getReturnType());
        return new MethodSignature(method.getName(), typeParameters, argumentTypes, returnType);
    }

    private static Identifier identifierFor(IMethodBinding method) {
        // TODO: disambiguate overloads
        return Identifier.forType(erasure(typeOf(method.getDeclaringClass()))).method(method.getName());
    }
}
