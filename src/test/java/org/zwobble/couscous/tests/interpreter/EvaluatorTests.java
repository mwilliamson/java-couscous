package org.zwobble.couscous.tests.interpreter;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.interpreter.*;
import org.zwobble.couscous.interpreter.values.IntegerInterpreterValue;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.StringInterpreterValue;
import org.zwobble.couscous.tests.BackendEvalTests;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.interpreter.Evaluator.eval;
import static org.zwobble.couscous.interpreter.values.InterpreterValues.value;
import static org.zwobble.couscous.tests.TestIds.ANY_ID;
import static org.zwobble.couscous.tests.util.ExtraAsserts.assertThrows;

public class EvaluatorTests extends BackendEvalTests {
    @Test
    public void valueOfAssignmentExpressionIsNewValue() {
        FormalArgumentNode arg = formalArg(var(ANY_ID, "x", StringValue.REF));
        Environment environment = new Environment(
            new MapBackedProject(ImmutableMap.of()),
            Optional.empty(),
            new StackFrameBuilder().declare(arg, value("[initial value]")).build());
        InterpreterValue result = eval(environment, assign(arg, literal("[updated value]")));
        assertEquals(new StringInterpreterValue("[updated value]"), result);
    }
    
    @Test
    public void errorIfConditionIsNotBoolean() {
        ConditionMustBeBoolean exception = assertThrows(ConditionMustBeBoolean.class,
            () -> eval(emptyEnvironment(),
                ternaryConditional(literal(1), literal("T"), literal("F"))));
        assertEquals(new ConditionMustBeBoolean(new IntegerInterpreterValue(1)), exception);
    }
    
    @Test
    public void errorIfMethodDoesNotExist() {
        NoSuchMethod exception = assertThrows(NoSuchMethod.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "size",
                    asList(),
                    IntegerValue.REF)));
        assertEquals(new MethodSignature("size", asList()), exception.getSignature());
    }
    
    @Test
    public void errorIfWrongNumberOfArgumentsArePassed() {
        NoSuchMethod exception = assertThrows(NoSuchMethod.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "substring",
                    asList(literal(1)),
                    StringValue.REF)));
        assertEquals(new MethodSignature("substring", asList(IntegerValue.REF)), exception.getSignature());
    }
    
    @Test
    public void errorIfArgumentIsWrongType() {
        NoSuchMethod exception = assertThrows(NoSuchMethod.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "substring",
                    asList(literal(0), literal("")),
                    StringValue.REF)));
        assertEquals(
            new MethodSignature("substring", asList(IntegerValue.REF, StringValue.REF)),
            exception.getSignature());
    }
    
    @Test
    public void errorIfConstructorArgumentIsWrongType() {
        FormalArgumentNode argument = formalArg(var(ANY_ID, "x", IntegerValue.REF));
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .constructor(constructor -> constructor
                .argument(argument))
            .build();
        
        UnexpectedValueType exception = assertThrows(UnexpectedValueType.class,
            () -> evalExpression(
                asList(classNode),
                constructorCall(
                    classNode.getName(),
                    asList(literal("")))));
        
        assertEquals(new UnexpectedValueType(IntegerValue.REF, StringValue.REF), exception);
    }
    
    @Test
    public void cannotGetValueOfUndeclaredField() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .build();
        
        NoSuchField exception = assertThrows(NoSuchField.class,
            () -> evalExpression(
                asList(classNode),
                fieldAccess(
                    constructorCall(
                        classNode.getName(),
                        asList()),
                    "value",
                    IntegerValue.REF)));
        
        assertEquals(new NoSuchField("value"), exception);
    }
    
    @Test
    public void cannotGetValueOfUnbounddField() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .field("value", IntegerValue.REF)
            .build();
        
        UnboundField exception = assertThrows(UnboundField.class,
            () -> evalExpression(
                asList(classNode),
                fieldAccess(
                    constructorCall(
                        classNode.getName(),
                        asList()),
                    "value",
                    IntegerValue.REF)));
        
        assertEquals(new UnboundField("value"), exception);
    }
    
    @Test
    public void cannotSetValueOfUndeclaredField() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .constructor(constructor -> constructor
                .statement(assignStatement(
                    fieldAccess(constructor.thisReference(), "value", IntegerValue.REF),
                    literal(42))))
            .build();
        
        NoSuchField exception = assertThrows(NoSuchField.class,
            () -> evalExpression(
                asList(classNode),
                constructorCall(
                    classNode.getName(),
                    asList())));
        
        assertEquals(new NoSuchField("value"), exception);
    }
    
    @Test
    public void cannotSetValueOfFieldWithWrongType() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .field("value", IntegerValue.REF)
            .constructor(constructor -> constructor
                .statement(assignStatement(
                    fieldAccess(constructor.thisReference(), "value", IntegerValue.REF),
                    literal(""))))
            .build();
        
        UnexpectedValueType exception = assertThrows(UnexpectedValueType.class,
            () -> evalExpression(
                asList(classNode),
                constructorCall(
                    classNode.getName(),
                    asList())));
        
        assertEquals(new UnexpectedValueType(IntegerValue.REF, StringValue.REF), exception);
    }
    
    private static Environment emptyEnvironment() {
        return new Environment(
            JavaProject.builder().build(),
            Optional.empty(),
            ImmutableMap.of());
    }

    @Override
    protected PrimitiveValue evalExpression(
            List<ClassNode> classes,
            ExpressionNode expression) {
        
        Environment environment = new Environment(
            JavaProject.of(classes),
            Optional.empty(),
            ImmutableMap.of());
        return eval(environment, expression).toPrimitiveValue().get();
    }
}
