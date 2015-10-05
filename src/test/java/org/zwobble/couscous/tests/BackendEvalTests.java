package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.values.PrimitiveValue;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.values.PrimitiveValues.value;

import lombok.val;

public abstract class BackendEvalTests {
    @Test
    public void whenConditionIsTrueThenValueOfConditionalTernaryIsTrueBranch() {
        val result = evalExpression(new TernaryConditionalNode(literal(true), literal("T"), literal("F")));
        assertEquals(value("T"), result);
    }
    
    @Test
    public void whenConditionIsFalseThenValueOfConditionalTernaryIsFalseBranch() {
        val result = evalExpression(new TernaryConditionalNode(literal(false), literal("T"), literal("F")));
        assertEquals(value("F"), result);
    }
    
    protected abstract PrimitiveValue evalExpression(ExpressionNode expression);
}