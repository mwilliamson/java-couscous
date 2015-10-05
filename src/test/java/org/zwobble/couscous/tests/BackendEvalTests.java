package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.StaticMethodCallNode.staticMethodCall;
import static org.zwobble.couscous.values.PrimitiveValues.value;

import lombok.val;

public abstract class BackendEvalTests {
    @Test
    public void canEvaluateLiterals() {
        assertEquals(value("hello"), evalExpression(literal("hello")));
        assertEquals(value(true), evalExpression(literal(true)));
        assertEquals(value(false), evalExpression(literal(false)));
        assertEquals(value(42), evalExpression(literal(42)));
    }
    
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
    
    @Test
    public void canCallMethodWithNoArgumentsOnBuiltin() {
        val result = evalExpression(methodCall(
            literal("hello"),
            "length",
            asList(),
            IntegerValue.REF));
        assertEquals(value(5), result);
    }
    
    @Test
    public void canCallMethodWithArgumentsOnBuiltin() {
        val result = evalExpression(methodCall(
            literal("hello"),
            "substring",
            asList(literal(1), literal(4)),
            StringValue.REF));
        assertEquals(value("ell"), result);
    }
    
    @Test
    public void canCallBuiltinStaticMethod() {
        val result = evalExpression(
            staticMethodCall("java.lang.Integer", "parseInt", literal("42")));
        assertEquals(value(42), result);
    }
    
    protected abstract PrimitiveValue evalExpression(ExpressionNode expression);
}
