package org.zwobble.couscous.tests.backends.csharp;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.tests.BackendEvalTests;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.List;

public class CsharpCompilerEvalTests extends BackendEvalTests {
    @Test
    @Ignore
    public void canCallMethodWithArgumentsOnBuiltin() {
    }

    @Test
    @Ignore
    public void canCallStaticMethodFromUserDefinedStaticMethod() {
    }

    @Override
    protected PrimitiveValue evalExpression(List<ClassNode> classes, ExpressionNode expression) {
        return new CsharpMethodRunner().evalExpression(classes, expression);
    }
}
