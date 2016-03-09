package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.types.*;
import org.zwobble.couscous.ast.sugar.Lambda;
import org.zwobble.couscous.ast.types.Type;

import java.util.List;
import java.util.Optional;

import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.sugar.Lambda.lambda;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaLambdaExpressionReader {
    private final JavaReader reader;

    public JavaLambdaExpressionReader(JavaReader reader) {
        this.reader = reader;
    }

    public Lambda toLambda(Scope scope, LambdaExpression expression) {
        List<FormalArgumentNode> formalArguments = eagerMap(
            (List<?>)expression.parameters(),
            parameter -> readLambdaExpressionParameter(scope, parameter));

        return lambda(formalArguments, readLambdaExpressionBody(scope, expression));
    }

    private FormalArgumentNode readLambdaExpressionParameter(Scope scope, Object parameter) {
        if (parameter instanceof SingleVariableDeclaration) {
            return JavaVariableDeclarationReader.read(scope, (SingleVariableDeclaration) parameter);
        } else {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment)parameter;
            return scope.formalArgument(
                fragment.resolveBinding().getKey(),
                fragment.getName().getIdentifier(),
                typeOf(fragment.resolveBinding().getType()));
        }
    }

    private List<StatementNode> readLambdaExpressionBody(Scope scope, LambdaExpression expression) {
        Type returnType = typeOf(expression.resolveTypeBinding().getFunctionalInterfaceMethod().getReturnType());
        if (expression.getBody() instanceof Block) {
            @SuppressWarnings("unchecked")
            List<Statement> statements = ((Block) expression.getBody()).statements();
            return reader.readStatements(scope, statements, Optional.of(returnType));
        } else {
            Expression body = (Expression) expression.getBody();
            return list(returns(reader.readExpression(scope, returnType, body)));
        }
    }
}
