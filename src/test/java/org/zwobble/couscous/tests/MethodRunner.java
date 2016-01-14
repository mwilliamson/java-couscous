package org.zwobble.couscous.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.list;

public interface MethodRunner {
    PrimitiveValue runMethod(
        List<ClassNode> classNodes,
        TypeName className,
        String methodName,
        List<PrimitiveValue> arguments);

    default PrimitiveValue evalExpression(List<ClassNode> classes, ExpressionNode expression) {
        ClassNode programNode = ClassNode.builder("Program")
            .method(MethodNode.staticMethod("run")
                .statement(ReturnNode.returns(expression))
                .build())
            .build();
        return runMethod(
            ImmutableList.copyOf(Iterables.concat(
                classes,
                list(programNode))),
            programNode.getName(),
            "run",
            list());
    }
}
