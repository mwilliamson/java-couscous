package org.zwobble.couscous.tests;

import java.util.List;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.StaticMethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.TestIds.ANY_ID;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public abstract class BackendEvalTests {
    @Test
    public void canEvaluateLiterals() {
        assertEquals(value("hello"), evalExpression(literal("hello")));
        assertEquals(value(true), evalExpression(literal(true)));
        assertEquals(value(false), evalExpression(literal(false)));
        assertEquals(value(42), evalExpression(literal(42)));
    }
    
    @Test
    public void canEvaluateOperationsOnIntegers() {
        assertEquals(
            value(3),
            evalExpression(methodCall(literal(1), "add", asList(literal(2)), IntegerValue.REF)));
        assertEquals(
            value(-1),
            evalExpression(methodCall(literal(1), "subtract", asList(literal(2)), IntegerValue.REF)));
        assertEquals(
            value(2),
            evalExpression(methodCall(literal(1), "multiply", asList(literal(2)), IntegerValue.REF)));
        
        assertEquals(value(0), evalExpression(divideIntegers(1, 2)));
        assertEquals(value(1), evalExpression(divideIntegers(10, 6)));
        assertEquals(value(-1), evalExpression(divideIntegers(-10, 6)));
        assertEquals(value(-1), evalExpression(divideIntegers(10, -6)));
        assertEquals(value(1), evalExpression(divideIntegers(-10, -6)));
        
        assertEquals(value(1), evalExpression(modIntegers(1, 2)));
        assertEquals(value(4), evalExpression(modIntegers(10, 6)));
        assertEquals(value(-4), evalExpression(modIntegers(-10, 6)));
        assertEquals(value(4), evalExpression(modIntegers(10, -6)));
        assertEquals(value(-4), evalExpression(modIntegers(-10, -6)));
        
        assertEquals(
            value(false),
            evalExpression(methodCall(literal(1), "greaterThan", asList(literal(2)), BooleanValue.REF)));
        assertEquals(
            value(false),
            evalExpression(methodCall(literal(1), "greaterThanOrEqual", asList(literal(2)), BooleanValue.REF)));
        assertEquals(
            value(true),
            evalExpression(methodCall(literal(1), "lessThan", asList(literal(2)), BooleanValue.REF)));
        assertEquals(
            value(true),
            evalExpression(methodCall(literal(1), "lessThanOrEqual", asList(literal(2)), BooleanValue.REF)));
    }

    private MethodCallNode divideIntegers(int left, int right) {
        return methodCall(literal(left), "divide", asList(literal(right)), IntegerValue.REF);
    }

    private MethodCallNode modIntegers(int left, int right) {
        return methodCall(literal(left), "mod", asList(literal(right)), IntegerValue.REF);
    }
    
    @Test
    public void whenConditionIsTrueThenValueOfConditionalTernaryIsTrueBranch() {
        PrimitiveValue result = evalExpression(ternaryConditional(literal(true), literal("T"), literal("F")));
        assertEquals(value("T"), result);
    }
    
    @Test
    public void whenConditionIsFalseThenValueOfConditionalTernaryIsFalseBranch() {
        PrimitiveValue result = evalExpression(ternaryConditional(literal(false), literal("T"), literal("F")));
        assertEquals(value("F"), result);
    }
    
    @Test
    public void canCallMethodWithNoArgumentsOnBuiltin() {
        PrimitiveValue result = evalExpression(methodCall(
            literal("hello"),
            "length",
            asList(),
            IntegerValue.REF));
        assertEquals(value(5), result);
    }
    
    @Test
    public void canCallMethodWithArgumentsOnBuiltin() {
        PrimitiveValue result = evalExpression(methodCall(
            literal("hello"),
            "substring",
            asList(literal(1), literal(4)),
            StringValue.REF));
        assertEquals(value("ell"), result);
    }
    
    @Test
    public void canCallBuiltinStaticMethod() {
        PrimitiveValue result = evalExpression(
            staticMethodCall("java.lang.Integer", "parseInt", literal("42")));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallStaticMethodFromUserDefinedStaticMethod() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .staticMethod("main", method -> method
                .statement(returns(staticMethodCall("java.lang.Integer", "parseInt", literal("42")))))
            .build();
        PrimitiveValue result = evalExpression(asList(classNode),
            staticMethodCall("com.example.Example", "main"));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallInstanceMethodWithNoArgumentsOnUserDefinedClass() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .method("main", method -> method
                .statement(returns(literal(42))))
            .build();
        PrimitiveValue result = evalExpression(asList(classNode),
            methodCall(constructorCall(classNode.getName(), asList()), "main", asList(), IntegerValue.REF));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallInstanceMethodWithArgumentsOnUserDefinedClass() {
        FormalArgumentNode argument = formalArg(var(ANY_ID, "x", IntegerValue.REF));
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .method("main", method -> method
                .argument(argument)
                .statement(returns(reference(argument))))
            .build();
        
        PrimitiveValue result = evalExpression(
            asList(classNode),
            methodCall(
                constructorCall(classNode.getName(), asList()),
                "main",
                asList(literal(42)),
                IntegerValue.REF));
        
        assertEquals(value(42), result);
    }
    
    @Test
    public void constructorIsExecutedOnConstruction() {
        FormalArgumentNode argument = formalArg(var(ANY_ID, "x", IntegerValue.REF));
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .field("value", IntegerValue.REF)
            .constructor(constructor -> constructor
                .argument(argument)
                .statement(assignStatement(
                    fieldAccess(constructor.thisReference(), "value", IntegerValue.REF),
                    reference(argument))))
            .method("main", method -> method
                .statement(returns(
                    fieldAccess(method.thisReference(), "value", IntegerValue.REF))))
            .build();
        
        PrimitiveValue result = evalExpression(
            asList(classNode),
            methodCall(
                constructorCall(classNode.getName(), asList(literal(42))),
                "main",
                asList(),
                IntegerValue.REF));
        
        assertEquals(value(42), result);
    }
    
    protected PrimitiveValue evalExpression(ExpressionNode expression) {
        return evalExpression(ImmutableList.of(), expression);
    }
    
    protected abstract PrimitiveValue evalExpression(
        List<ClassNode> classes,
        ExpressionNode expression);
}
