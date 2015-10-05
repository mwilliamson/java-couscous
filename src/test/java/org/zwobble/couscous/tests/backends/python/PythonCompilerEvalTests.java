package org.zwobble.couscous.tests.backends.python;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.tests.BackendEvalTests;
import org.zwobble.couscous.values.PrimitiveValue;

import com.google.common.collect.ImmutableList;

import lombok.val;

public class PythonCompilerEvalTests extends BackendEvalTests {
    @Override
    @Test
    @Ignore("Work in progress")
    public void canCallMethodWithNoArgumentsOnBuiltin() {
        
    }
    
    @Override
    protected PrimitiveValue evalExpression(ExpressionNode expression) {
        val runner = new PythonMethodRunner();
        return runner.runMethod(
            ClassNode.builder("Program")
                .method(MethodNode.staticMethod("run")
                    .statement(new ReturnNode(expression))
                    .build())
                .build(),
            "run",
            ImmutableList.of());
    }
}
