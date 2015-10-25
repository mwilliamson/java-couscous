package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodNode.staticMethod;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.TestIds.ANY_ID;
import static org.zwobble.couscous.values.PrimitiveValues.value;

import lombok.val;

public abstract class BackendMethodTests {
    @Test
    public void methodWithNoStatementsReturnsUnit() {
        val method = staticMethod("hello");
        val result = runMethod(method);
        
        assertEquals(PrimitiveValues.UNIT, result);
    }
    
    @Test
    public void canReturnLiteralValue() {
        val method = staticMethod("hello")
            .statement(new ReturnNode(literal("hello, world!")));
        val result = runMethod(method);
        
        assertEquals(value("hello, world!"), result);
    }
    @Test
    public void canPassValueToMethod() {
        val arg = formalArg(var(ANY_ID, "x", StringValue.REF));
        val method = staticMethod("hello")
            .argument(arg)
            .statement(new ReturnNode(reference(arg)));
        val result = runMethod(method, value("hello, world!"));
        
        assertEquals(value("hello, world!"), result);
    }
    
    @Test
    public void canReassignValueToArgument() {
        val arg = formalArg(var(ANY_ID, "x", StringValue.REF));
        val method = staticMethod("hello")
            .argument(arg)
            .statement(new ExpressionStatementNode(assign(reference(arg), LiteralNode.literal("[updated value]"))))
            .statement(new ReturnNode(reference(arg)));
        val result = runMethod(method, value("[initial value]"));
        
        assertEquals(value("[updated value]"), result);
    }
    
    @Test
    public void canDeclareVariable() {
        val localVariableDeclaration = localVariableDeclaration(
            ANY_ID, "x", StringValue.REF, LiteralNode.literal("[initial value]"));
        val method = staticMethod("hello")
            .statement(localVariableDeclaration)
            .statement(new ReturnNode(reference(localVariableDeclaration)));
        val result = runMethod(method);
        
        assertEquals(value("[initial value]"), result);
    }
    
    @Test
    public void canDeclareVariableAndThenAssignValues() {
        val localVariableDeclaration = localVariableDeclaration(
            ANY_ID, "x", StringValue.REF, LiteralNode.literal("[initial value]"));
        val method = staticMethod("hello")
            .statement(localVariableDeclaration)
            .statement(new ExpressionStatementNode(assign(reference(localVariableDeclaration), LiteralNode.literal("[updated value]"))))
            .statement(new ReturnNode(reference(localVariableDeclaration)));
        val result = runMethod(method);
        
        assertEquals(value("[updated value]"), result);
    }

    protected PrimitiveValue runMethod(MethodNode.MethodNodeBuilder methodBuilder, PrimitiveValue... arguments) {
        val method = methodBuilder.build();
        val className = "com.example.Program";
        val classNode = ClassNode.builder(className)
            .method(method)
            .build();
        
        val runner = buildMethodRunner();
        return runner.runMethod(
            asList(classNode),
            classNode.getName(),
            method.getName(),
            asList(arguments));
    }
    
    protected abstract MethodRunner buildMethodRunner();
        
}
