package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.VariableReferenceNode;
import org.zwobble.couscous.ast.sugar.Lambda;

import java.util.List;

import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.StaticMethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.sugar.Lambda.lambda;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class ExpressionMethodReferenceReader {
    public static Lambda toLambda(ExpressionMethodReference expression) {
        IMethodBinding functionalInterfaceMethod = expression.resolveTypeBinding().getFunctionalInterfaceMethod();
        List<FormalArgumentNode> formalArguments = formalArguments(functionalInterfaceMethod);

        return lambda(
            formalArguments,
            ImmutableList.of(returns(JavaExpressionReader.handleBoxing(
                typeOf(functionalInterfaceMethod.getReturnType()),
                staticMethodCall(
                    typeOf(expression.resolveMethodBinding().getDeclaringClass()),
                    expression.getName().getIdentifier(),
                    eagerMap(formalArguments, VariableReferenceNode::reference),
                    typeOf(expression.resolveMethodBinding().getReturnType()))))));
    }

    private static List<FormalArgumentNode> formalArguments(IMethodBinding functionalInterfaceMethod) {
        ImmutableList.Builder<FormalArgumentNode> arguments = ImmutableList.builder();

        ITypeBinding[] parameterTypes = functionalInterfaceMethod.getParameterTypes();
        for (int index = 0; index < parameterTypes.length; index++) {
            ITypeBinding parameterType = parameterTypes[index];
            arguments.add(formalArg(var("arg" + index, "arg" + index, typeOf(parameterType))));
        }

        return arguments.build();
    }
}
