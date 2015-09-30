package org.zwobble.couscous.tests.interpreter;

import org.junit.Test;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.interpreter.ConditionMustBeBoolean;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.NoSuchMethod;
import org.zwobble.couscous.interpreter.UnexpectedValueType;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.Assignment.assign;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.interpreter.Evaluator.eval;
import static org.zwobble.couscous.tests.util.ExtraAsserts.assertThrows;

import lombok.val;

public class EvaluatorTests {
    @Test
    public void valueOfAssignmentExpressionIsNewValue() {
        val arg = new FormalArgumentNode(42, "x");
        val environment = new Environment(ImmutableMap.of(arg.getId(), new StringValue("[initial value]")));
        val result = eval(environment, assign(arg, literal("[updated value]")));
        assertEquals(new StringValue("[updated value]"), result);
    }
    
    @Test
    public void whenConditionIsTrueThenValueOfConditionalTernaryIsTrueBranch() {
        val result = eval(emptyEnvironment(),
            new TernaryConditionalNode(literal(true), literal("T"), literal("F")));
        assertEquals(new StringValue("T"), result);
    }
    
    @Test
    public void whenConditionIsFalseThenValueOfConditionalTernaryIsFalseBranch() {
        val result = eval(emptyEnvironment(),
            new TernaryConditionalNode(literal(false), literal("T"), literal("F")));
        assertEquals(new StringValue("F"), result);
    }
    
    @Test
    public void errorIfConditionIsNotBoolean() {
        val exception = assertThrows(ConditionMustBeBoolean.class,
            () -> eval(emptyEnvironment(),
                new TernaryConditionalNode(literal(1), literal("T"), literal("F"))));
        assertEquals(new ConditionMustBeBoolean(new IntegerValue(1)), exception);
    }
    
    @Test
    public void canCallMethodWithNoArgumentsOnBuiltin() {
        val result = eval(emptyEnvironment(),
            methodCall(literal("hello"), "length"));
        assertEquals(new IntegerValue(5), result);
    }
    
    @Test
    public void canCallMethodWithArgumentsOnBuiltin() {
        val result = eval(emptyEnvironment(),
            methodCall(literal("hello"), "substring", literal(1), literal(4)));
        assertEquals(new StringValue("ell"), result);
    }
    
    @Test
    public void errorIfMethodDoesNotExist() {
        val exception = assertThrows(NoSuchMethod.class,
            () -> eval(emptyEnvironment(),
                methodCall(literal("hello"), "size")));
        assertEquals(new NoSuchMethod("size"), exception);
    }
    
    @Test
    public void errorIfWrongNumberOfArgumentsArePassed() {
        val exception = assertThrows(WrongNumberOfArguments.class,
            () -> eval(emptyEnvironment(),
                methodCall(literal("hello"), "substring", literal(1))));
        assertEquals(new WrongNumberOfArguments(2, 1), exception);
    }
    
    @Test
    public void errorIfArgumentIsWrongType() {
        val exception = assertThrows(UnexpectedValueType.class,
            () -> eval(emptyEnvironment(),
                methodCall(literal("hello"), "substring", literal(0), literal(""))));
        assertEquals(new UnexpectedValueType(IntegerValue.TYPE, StringValue.TYPE), exception);
    }
    
    private static Environment emptyEnvironment() {
        return new Environment(ImmutableMap.of());
    }
}
