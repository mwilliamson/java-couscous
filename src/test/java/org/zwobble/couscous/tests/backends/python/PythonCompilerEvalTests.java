package org.zwobble.couscous.tests.backends.python;

import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.tests.BackendEvalTests;
import org.zwobble.couscous.values.PrimitiveValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import static java.util.Arrays.asList;

import lombok.val;

public class PythonCompilerEvalTests extends BackendEvalTests {
    @Override
    protected PrimitiveValue evalExpression(
            List<ClassNode> classes,
            ExpressionNode expression) {
        
        val programNode = ClassNode.builder("Program")
            .method(MethodNode.staticMethod("run")
                .statement(ReturnNode.returns(expression))
                .build())
            .build();
        
        val runner = new PythonMethodRunner();
        
        return runner.runMethod(
            ImmutableList.copyOf(Iterables.concat(classes, asList(programNode))),
            programNode.getName(),
            "run",
            ImmutableList.of());
    }
}
