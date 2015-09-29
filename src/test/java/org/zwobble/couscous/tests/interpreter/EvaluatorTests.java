package org.zwobble.couscous.tests.interpreter;

import org.junit.Test;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.Assignment.assign;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.interpreter.Evaluator.eval;

import lombok.val;

public class EvaluatorTests {
    @Test
    public void valueOfAssignmentExpressionIsNewValue() {
        val arg = new FormalArgumentNode(42, "x");
        val environment = new Environment(ImmutableMap.of(arg.getId(), new StringValue("[initial value]")));
        val result = eval(environment, assign(arg, literal("[updated value]")));
        assertEquals(new StringValue("[updated value]"), result);
    }
}
