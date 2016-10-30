package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.Operations.*;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.Operations.same;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.TestIds.ANY_ID;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public abstract class BackendEvalTests {
    @Test
    public void canEvaluateLiterals() {
        assertEquals(value("hello"), evalExpression(literal("hello")));
        assertEquals(value(true), evalExpression(literal(true)));
        assertEquals(value(false), evalExpression(literal(false)));
        assertEquals(value(42), evalExpression(literal(42)));
        assertEquals(value(Types.STRING), evalExpression(literal(Types.STRING)));
    }
    
    @Test
    public void equalityOnReferenceTypesChecksForIdentity() {
        assertEquals(
            value(false),
            evalExpression(same(
                constructorCall(ScalarType.topLevel("java.lang.Object"), Collections.emptyList()),
                constructorCall(ScalarType.topLevel("java.lang.Object"), Collections.emptyList()))));
    }
    
    @Test
    public void canEvaluateOperationsOnBooleans() {
        assertEquals(
            value(true),
            evalExpression(not(literal(false))));
        assertEquals(
            value(false),
            evalExpression(not(literal(true))));
    }
    
    @Test
    public void canEvaluateOperationsOnIntegers() {
        assertEquals(
            value(3),
            evalExpression(integerAdd(literal(1), literal(2))));
        assertEquals(
            value(-1),
            evalExpression(integerSubtract(literal(1), literal(2))));
        assertEquals(
            value(2),
            evalExpression(integerMultiply(literal(1), literal(2))));
        
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
            evalExpression(equal(literal(1), literal(2))));
        assertEquals(
            value(true),
            evalExpression(equal(literal(1), literal(1))));
        assertEquals(
            value(false),
            evalExpression(greaterThan(literal(1), literal(2))));
        assertEquals(
            value(false),
            evalExpression(greaterThanOrEqual(literal(1), literal(2))));
        assertEquals(
            value(true),
            evalExpression(lessThan(literal(1), literal(2))));
        assertEquals(
            value(true),
            evalExpression(lessThanOrEqual(literal(1), literal(2))));
    }

    private ExpressionNode divideIntegers(int left, int right) {
        return integerDivide(literal(left), literal(right));
    }

    private ExpressionNode modIntegers(int left, int right) {
        return integerMod(literal(left), literal(right));
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
            list(),
            Types.INT));
        assertEquals(value(5), result);
    }
    
    @Test
    public void canCallMethodWithArgumentsOnBuiltin() {
        PrimitiveValue result = evalExpression(methodCall(
            literal("hello"),
            "substring",
            list(literal(1), literal(4)),
            Types.STRING));
        assertEquals(value("ell"), result);
    }
    
    @Test
    public void canCallBuiltinStaticMethod() {
        PrimitiveValue result = evalExpression(staticMethodCall(
            "java.lang.Integer",
            "parseInt",
            list(literal("42")),
            Types.INT));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallStaticMethodFromUserDefinedStaticMethod() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .staticMethod("main", method -> method
                .returns(Types.INT)
                .statement(returns(staticMethodCall(
                    "java.lang.Integer",
                    "parseInt",
                    list(literal("42")),
                    Types.INT))))
            .build();
        PrimitiveValue result = evalExpression(list(classNode),
            staticMethodCall("com.example.Example", "main", list(), Types.INT));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallInstanceMethodWithNoArgumentsOnUserDefinedClass() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .method("main", method -> method
                .returns(Types.INT)
                .statement(returns(literal(42))))
            .build();
        PrimitiveValue result = evalExpression(list(classNode),
            methodCall(constructorCall(classNode.getName(), list()), "main", list(), Types.INT));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallInstanceMethodWithArgumentsOnUserDefinedClass() {
        FormalArgumentNode argument = formalArg(var(ANY_ID, "x", Types.INT));
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .method("main", method -> method
                .argument(argument)
                .returns(Types.INT)
                .statement(returns(reference(argument))))
            .build();
        
        PrimitiveValue result = evalExpression(
            list(classNode),
            methodCall(
                constructorCall(classNode.getName(), list()),
                "main",
                list(literal(42)),
                Types.INT));
        
        assertEquals(value(42), result);
    }
    
    @Test
    public void constructorIsExecutedOnConstruction() {
        FormalArgumentNode argument = formalArg(var(ANY_ID, "x", Types.INT));
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .field("value", Types.INT)
            .constructor(constructor -> constructor
                .argument(argument)
                .statement(assignStatement(
                    fieldAccess(constructor.thisReference(), "value", Types.INT),
                    reference(argument))))
            .method("main", method -> method
                .returns(Types.INT)
                .statement(returns(
                    fieldAccess(method.thisReference(), "value", Types.INT))))
            .build();
        
        PrimitiveValue result = evalExpression(
            list(classNode),
            methodCall(
                constructorCall(classNode.getName(), list(literal(42))),
                "main",
                list(),
                Types.INT));
        
        assertEquals(value(42), result);
    }

    @Test
    public void staticConstructorIsExecutedOnReference() {
        ScalarType type = ScalarType.topLevel("com.example.Example");
        ClassNode classNode = ClassNode.builder(type)
            .staticField("value", Types.INT)
            .staticConstructor(list(
                assignStatement(
                    fieldAccess(type, "value", Types.INT),
                    literal(42))))
            .build();

        PrimitiveValue result = evalExpression(
            list(classNode),
            fieldAccess(type, "value", Types.INT));

        assertEquals(value(42), result);
    }
    
    private PrimitiveValue evalExpression(ExpressionNode expression) {
        return evalExpression(list(), expression);
    }
    
    protected abstract PrimitiveValue evalExpression(
        List<TypeNode> classes,
        ExpressionNode expression);
}
