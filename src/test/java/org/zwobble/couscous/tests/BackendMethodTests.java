package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.Operations.integerAdd;
import static org.zwobble.couscous.ast.Operations.notEqual;
import static org.zwobble.couscous.ast.MethodNode.staticMethod;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.tests.TestIds.ANY_ID;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public abstract class BackendMethodTests {
    @Test
    public void methodWithNoStatementsReturnsUnit() {
        MethodNode.Builder method = staticMethod("hello");
        final java.lang.Object result = runMethod(method);
        assertEquals(PrimitiveValues.UNIT, result);
    }
    
    @Test
    public void canReturnLiteralValue() {
        MethodNode.Builder method = staticMethod("hello")
            .statement(returns(literal("hello, world!")));
        final java.lang.Object result = runMethod(method);
        assertEquals(value("hello, world!"), result);
    }
    
    @Test
    public void canPassValueToMethod() {
        FormalArgumentNode arg = formalArg(var(ANY_ID, "x", StringValue.REF));
        MethodNode.Builder method = staticMethod("hello")
            .argument(arg)
            .statement(returns(reference(arg)));
        final java.lang.Object result = runMethod(method, value("hello, world!"));
        assertEquals(value("hello, world!"), result);
    }
    
    @Test
    public void canReassignValueToArgument() {
        FormalArgumentNode arg = formalArg(var(ANY_ID, "x", StringValue.REF));
        MethodNode.Builder method = staticMethod("hello")
            .argument(arg)
            .statement(expressionStatement(assign(
                reference(arg),
                literal("[updated value]"))))
            .statement(returns(reference(arg)));
        final java.lang.Object result = runMethod(method, value("[initial value]"));
        assertEquals(value("[updated value]"), result);
    }
    
    @Test
    public void canDeclareVariable() {
        LocalVariableDeclarationNode localVariableDeclaration = localVariableDeclaration(ANY_ID, "x", StringValue.REF, LiteralNode.literal("[initial value]"));
        MethodNode.Builder method = staticMethod("hello")
            .statement(localVariableDeclaration)
            .statement(returns(reference(localVariableDeclaration)));
        final java.lang.Object result = runMethod(method);
        assertEquals(value("[initial value]"), result);
    }
    
    @Test
    public void canDeclareVariableAndThenAssignValues() {
        LocalVariableDeclarationNode localVariableDeclaration = localVariableDeclaration(ANY_ID, "x", StringValue.REF, LiteralNode.literal("[initial value]"));
        MethodNode.Builder method = staticMethod("hello")
            .statement(localVariableDeclaration)
            .statement(expressionStatement(assign(
                reference(localVariableDeclaration),
                literal("[updated value]"))))
            .statement(returns(reference(localVariableDeclaration)));
        final java.lang.Object result = runMethod(method);
        assertEquals(value("[updated value]"), result);
    }
    
    @Test
    public void whenConditionIsTrueIfStatementExecutesTrueBranch() {
        MethodNode.Builder method = staticMethod("hello")
            .statement(ifStatement(
                literal(true),
                list(returns(literal("[true]"))),
                list(returns(literal("[false]")))));
        assertEquals(value("[true]"), runMethod(method));
    }

    @Test
    public void whenConditionIsFalseIfStatementExecutesTrueBranch() {
        MethodNode.Builder method = staticMethod("hello")
            .statement(ifStatement(
                literal(false),
                list(returns(literal("[true]"))),
                list(returns(literal("[false]")))));
        assertEquals(value("[false]"), runMethod(method));
    }

    @Test
    public void whileLoopIsExecutedWhileConditionIsTrue() {
        LocalVariableDeclarationNode x = localVariableDeclaration(TestIds.id("x"), "x", IntegerValue.REF, literal(0));
        LocalVariableDeclarationNode y = localVariableDeclaration(TestIds.id("y"), "y", IntegerValue.REF, literal(2));
        MethodNode.Builder method = staticMethod("hello")
            .statement(x)
            .statement(y)
            .statement(whileLoop(
                notEqual(reference(x), reference(y)),
                list(assignStatement(x, integerAdd(reference(x), literal(1))))))
            .statement(returns(reference(x)));
        assertEquals(value(2), runMethod(method));
    }

    @Test
    public void returnWillExitWhileLoopEarly() {
        LocalVariableDeclarationNode x = localVariableDeclaration(TestIds.id("x"), "x", IntegerValue.REF, literal(0));
        LocalVariableDeclarationNode y = localVariableDeclaration(TestIds.id("y"), "y", IntegerValue.REF, literal(2));
        MethodNode.Builder method = staticMethod("hello")
            .statement(x)
            .statement(y)
            .statement(whileLoop(
                notEqual(reference(x), reference(y)),
                list(
                    assignStatement(x, integerAdd(reference(x), literal(1))),
                    returns(reference(x)))))
            .statement(returns(reference(x)));
        assertEquals(value(1), runMethod(method));
    }
    
    protected PrimitiveValue runMethod(MethodNode.Builder methodBuilder, PrimitiveValue... arguments) {
        MethodNode method = methodBuilder.build();
        String className = "com.example.Program";
        ClassNode classNode = ClassNode.builder(className).method(method).build();
        MethodRunner runner = buildMethodRunner();
        return runner.runMethod(list(classNode), classNode.getName(), method.getName(), asList(arguments));
    }
    
    protected abstract MethodRunner buildMethodRunner();
}