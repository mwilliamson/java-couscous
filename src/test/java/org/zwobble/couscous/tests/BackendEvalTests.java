package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

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
        assertEquals(value(StringValue.REF), evalExpression(literal(StringValue.REF)));
    }
    
    @Test
    public void equalityOnReferenceTypesChecksForIdentity() {
        assertEquals(
            value(false),
            evalExpression(same(
                constructorCall(TypeName.of("java.lang.Object"), Collections.emptyList()),
                constructorCall(TypeName.of("java.lang.Object"), Collections.emptyList()))));
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
            IntegerValue.REF));
        assertEquals(value(5), result);
    }
    
    @Test
    public void canCallMethodWithArgumentsOnBuiltin() {
        PrimitiveValue result = evalExpression(methodCall(
            literal("hello"),
            "substring",
            list(literal(1), literal(4)),
            StringValue.REF));
        assertEquals(value("ell"), result);
    }
    
    @Test
    public void canCallBuiltinStaticMethod() {
        PrimitiveValue result = evalExpression(staticMethodCall(
            "java.lang.Integer",
            "parseInt",
            list(literal("42")),
            IntegerValue.REF));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallStaticMethodFromUserDefinedStaticMethod() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .staticMethod("main", method -> method
                .returns(IntegerValue.REF)
                .statement(returns(staticMethodCall(
                    "java.lang.Integer",
                    "parseInt",
                    list(literal("42")),
                    IntegerValue.REF))))
            .build();
        PrimitiveValue result = evalExpression(list(classNode),
            staticMethodCall("com.example.Example", "main", list(), IntegerValue.REF));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallInstanceMethodWithNoArgumentsOnUserDefinedClass() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .method("main", method -> method
                .returns(IntegerValue.REF)
                .statement(returns(literal(42))))
            .build();
        PrimitiveValue result = evalExpression(list(classNode),
            methodCall(constructorCall(classNode.getName(), list()), "main", list(), IntegerValue.REF));
        assertEquals(value(42), result);
    }
    
    @Test
    public void canCallInstanceMethodWithArgumentsOnUserDefinedClass() {
        FormalArgumentNode argument = formalArg(var(ANY_ID, "x", IntegerValue.REF));
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .method("main", method -> method
                .argument(argument)
                .returns(IntegerValue.REF)
                .statement(returns(reference(argument))))
            .build();
        
        PrimitiveValue result = evalExpression(
            list(classNode),
            methodCall(
                constructorCall(classNode.getName(), list()),
                "main",
                list(literal(42)),
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
                .returns(IntegerValue.REF)
                .statement(returns(
                    fieldAccess(method.thisReference(), "value", IntegerValue.REF))))
            .build();
        
        PrimitiveValue result = evalExpression(
            list(classNode),
            methodCall(
                constructorCall(classNode.getName(), list(literal(42))),
                "main",
                list(),
                IntegerValue.REF));
        
        assertEquals(value(42), result);
    }

    @Test
    public void staticConstructorIsExecutedOnReference() {
        TypeName type = TypeName.of("com.example.Example");
        ClassNode classNode = ClassNode.builder(type)
            .staticField("value", IntegerValue.REF)
            .staticConstructor(list(
                assignStatement(
                    fieldAccess(type, "value", IntegerValue.REF),
                    literal(42))))
            .build();

        PrimitiveValue result = evalExpression(
            list(classNode),
            fieldAccess(type, "value", IntegerValue.REF));

        assertEquals(value(42), result);
    }
    
    private PrimitiveValue evalExpression(ExpressionNode expression) {
        return evalExpression(list(), expression);
    }
    
    protected abstract PrimitiveValue evalExpression(
        List<ClassNode> classes,
        ExpressionNode expression);
}
