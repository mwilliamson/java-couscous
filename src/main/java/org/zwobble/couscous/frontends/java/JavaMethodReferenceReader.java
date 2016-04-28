package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.sugar.Lambda;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

import java.util.List;
import java.util.function.Function;

import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.TypeCoercionNode.coerce;
import static org.zwobble.couscous.ast.sugar.Lambda.lambda;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaMethodReferenceReader {
    private final JavaReader javaReader;

    public JavaMethodReferenceReader(JavaReader javaReader) {
        this.javaReader = javaReader;
    }

    public Lambda toLambda(Scope scope, ExpressionMethodReference expression) {
        return toLambda(
            scope,
            expression,
            arguments -> generateValue(scope, expression, arguments));
    }

    public Lambda toLambda(Scope scope, CreationReference expression) {
        return toLambda(
            scope,
            expression,
            arguments -> constructorCall(typeOf(expression.getType()), arguments));
    }

    private Lambda toLambda(
        Scope scope,
        MethodReference expression,
        Function<List<ExpressionNode>, ExpressionNode> generateValue)
    {
        IMethodBinding functionalInterfaceMethod = expression.resolveTypeBinding().getFunctionalInterfaceMethod();
        List<FormalArgumentNode> formalArguments = formalArguments(scope, functionalInterfaceMethod);
        List<ExpressionNode> arguments = eagerMap(formalArguments, VariableReferenceNode::reference);

        ExpressionNode value = generateValue.apply(arguments);
        Type type = typeOf(functionalInterfaceMethod.getReturnType());
        List<StatementNode> body = type.equals(Types.VOID)
            ? list(expressionStatement(value))
            : list(returns(coerce(value, type)));
        return lambda(formalArguments, body);
    }

    private List<FormalArgumentNode> formalArguments(Scope scope, IMethodBinding functionalInterfaceMethod) {
        ImmutableList.Builder<FormalArgumentNode> arguments = ImmutableList.builder();

        ITypeBinding[] parameterTypes = functionalInterfaceMethod.getParameterTypes();
        for (int index = 0; index < parameterTypes.length; index++) {
            ITypeBinding parameterType = parameterTypes[index];
            arguments.add(scope.formalArgument("arg" + index, typeOf(parameterType)));
        }

        return arguments.build();
    }

    private ExpressionNode generateValue(Scope scope, ExpressionMethodReference expression, List<ExpressionNode> arguments) {
        IMethodBinding methodBinding = expression.resolveMethodBinding();
        String methodName = expression.getName().getIdentifier();
        Type type = typeOf(methodBinding.getReturnType());

        if (Modifier.isStatic(methodBinding.getModifiers())) {
            return staticMethodCall(
                Types.erasure(typeOf(methodBinding.getDeclaringClass())),
                methodName,
                arguments,
                type);
        } else {
            Expression left = expression.getExpression();
            if (left instanceof Name && ((Name) left).resolveBinding().getKind() == IBinding.TYPE) {
                return methodCall(
                    arguments.get(0),
                    methodName,
                    arguments.subList(1, arguments.size()),
                    type);
            } else {
                return methodCall(
                    javaReader.readExpressionWithoutBoxing(scope, left),
                    methodName,
                    arguments,
                    type);
            }
        }
    }
}
