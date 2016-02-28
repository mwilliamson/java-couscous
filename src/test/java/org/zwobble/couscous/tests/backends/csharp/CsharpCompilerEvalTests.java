package org.zwobble.couscous.tests.backends.csharp;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.tests.BackendEvalTests;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.List;

public class CsharpCompilerEvalTests extends BackendEvalTests {
    @Override
    protected PrimitiveValue evalExpression(List<TypeNode> classes, ExpressionNode expression) {
        return new CsharpMethodRunner().evalExpression(classes, expression);
    }
}
