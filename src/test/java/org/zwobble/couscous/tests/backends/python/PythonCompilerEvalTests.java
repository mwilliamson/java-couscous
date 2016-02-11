package org.zwobble.couscous.tests.backends.python;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.tests.BackendEvalTests;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.List;

public class PythonCompilerEvalTests extends BackendEvalTests {
    @Ignore
    @Test
    public void staticConstructorIsExecutedOnReference() {
    }

    @Override
    protected PrimitiveValue evalExpression(List<ClassNode> classes, ExpressionNode expression) {
        return new PythonMethodRunner().evalExpression(classes, expression);
    }
}