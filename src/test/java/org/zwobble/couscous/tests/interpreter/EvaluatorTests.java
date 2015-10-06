package org.zwobble.couscous.tests.interpreter;

import java.util.List;

import org.junit.Test;
import org.zwobble.couscous.JavaProject;
import org.zwobble.couscous.MapBackedProject;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.interpreter.ConditionMustBeBoolean;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.NoSuchMethod;
import org.zwobble.couscous.interpreter.StackFrameBuilder;
import org.zwobble.couscous.interpreter.UnexpectedValueType;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;
import org.zwobble.couscous.interpreter.values.IntegerInterpreterValue;
import org.zwobble.couscous.interpreter.values.StringInterpreterValue;
import org.zwobble.couscous.tests.BackendEvalTests;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.interpreter.Evaluator.eval;
import static org.zwobble.couscous.interpreter.values.InterpreterValues.value;
import static org.zwobble.couscous.tests.util.ExtraAsserts.assertThrows;

import lombok.val;

public class EvaluatorTests extends BackendEvalTests {
    @Test
    public void valueOfAssignmentExpressionIsNewValue() {
        val arg = formalArg(var(42, "x", StringValue.REF));
        val environment = new Environment(
            new MapBackedProject(ImmutableMap.of()),
            new StackFrameBuilder().declare(arg, value("[initial value]")).build());
        val result = eval(environment, assign(arg, literal("[updated value]")));
        assertEquals(new StringInterpreterValue("[updated value]"), result);
    }
    
    @Test
    public void errorIfConditionIsNotBoolean() {
        val exception = assertThrows(ConditionMustBeBoolean.class,
            () -> eval(emptyEnvironment(),
                new TernaryConditionalNode(literal(1), literal("T"), literal("F"))));
        assertEquals(new ConditionMustBeBoolean(new IntegerInterpreterValue(1)), exception);
    }
    
    @Test
    public void errorIfMethodDoesNotExist() {
        val exception = assertThrows(NoSuchMethod.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "size",
                    asList(),
                    IntegerValue.REF)));
        assertEquals(new NoSuchMethod("size"), exception);
    }
    
    @Test
    public void errorIfWrongNumberOfArgumentsArePassed() {
        val exception = assertThrows(WrongNumberOfArguments.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "substring",
                    asList(literal(1)),
                    StringValue.REF)));
        assertEquals(new WrongNumberOfArguments(2, 1), exception);
    }
    
    @Test
    public void errorIfArgumentIsWrongType() {
        val exception = assertThrows(UnexpectedValueType.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "substring",
                    asList(literal(0), literal("")),
                    StringValue.REF)));
        assertEquals(new UnexpectedValueType(IntegerValue.REF, StringValue.REF), exception);
    }
    
    private static Environment emptyEnvironment() {
        return new Environment(
            JavaProject.builder().build(),
            ImmutableMap.of());
    }

    @Override
    protected PrimitiveValue evalExpression(
            List<ClassNode> classes,
            ExpressionNode expression) {
        
        val environment = new Environment(
            JavaProject.of(classes),
            ImmutableMap.of());
        return eval(environment, expression).toPrimitiveValue().get();
    }
}
