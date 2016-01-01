package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.sugar.Lambda;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.sugar.Lambda.lambda;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class JavaLambdaExpressionReader {
    private final JavaReader reader;

    public JavaLambdaExpressionReader(JavaReader reader) {
        this.reader = reader;
    }

    public Lambda javaLambdaToLambda(LambdaExpression expression) {
        List<FormalArgumentNode> formalArguments = eagerMap(
            (List<?>)expression.parameters(),
            this::readLambdaExpressionParameter);

        return lambda(formalArguments, readLambdaExpressionBody(expression));
    }

    private FormalArgumentNode readLambdaExpressionParameter(Object parameter) {
        if (parameter instanceof SingleVariableDeclaration) {
            return JavaVariableDeclarationReader.read((SingleVariableDeclaration) parameter);
        } else {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment)parameter;
            return formalArg(var(
                fragment.resolveBinding().getKey(),
                fragment.getName().getIdentifier(),
                typeOf(fragment.resolveBinding().getType())));
        }
    }

    private List<StatementNode> readLambdaExpressionBody(LambdaExpression expression) {
        TypeName returnType = typeOf(expression.resolveTypeBinding().getFunctionalInterfaceMethod().getReturnType());
        if (expression.getBody() instanceof Block) {
            @SuppressWarnings("unchecked")
            List<Statement> statements = ((Block) expression.getBody()).statements();
            return reader.readStatements(statements, Optional.of(returnType));
        } else {
            Expression body = (Expression) expression.getBody();
            return asList(returns(reader.readExpression(returnType, body)));
        }
    }
}
