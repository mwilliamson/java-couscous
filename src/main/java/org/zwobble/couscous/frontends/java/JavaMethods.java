package org.zwobble.couscous.frontends.java;

import com.google.common.collect.Iterables;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.types.Type;

import java.util.List;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

class JavaMethods {
    static MethodSignature signature(IMethodBinding method) {
        ITypeBinding declaringClass = method.getDeclaringClass();
        ITypeBinding erasedDeclaringClass = declaringClass.getErasure();

        List<Type> argumentTypes = eagerMap(asList(method.getParameterTypes()), JavaTypes::typeOf);
        Type returnType = typeOf(method.getReturnType());

        if (declaringClass.isEqualTo(erasedDeclaringClass)) {
            return new MethodSignature(method.getName(), argumentTypes, returnType);
        } else {
            IMethodBinding[] erasedMethods = erasedDeclaringClass.getDeclaredMethods();
            IMethodBinding erasedMethod = Iterables.getOnlyElement(Iterables.filter(
                asList(erasedMethods),
                m -> m.isSubsignature(method)));
            return new MethodSignature(
                method.getName(),
                eagerMap(
                    argumentTypes,
                    asList(erasedMethod.getParameterTypes()),
                    (argumentType, erasedParameterType) -> JavaTypes.bind(typeOf(erasedParameterType), argumentType)),
                JavaTypes.bind(typeOf(erasedMethod.getReturnType()), returnType));
        }
    }

}
