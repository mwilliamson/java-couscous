package org.zwobble.couscous.tests.interpreter;

import org.junit.Test;
import org.zwobble.couscous.MapBackedProject;
import org.zwobble.couscous.ast.Assignment;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.interpreter.Interpreter;
import org.zwobble.couscous.interpreter.VariableNotInScope;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;
import org.zwobble.couscous.values.ConcreteType;
import org.zwobble.couscous.values.InterpreterValue;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodNode.staticMethod;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.util.ExtraAsserts.assertThrows;
import static org.zwobble.couscous.values.UnitValue.UNIT;

import lombok.val;

public class InterpreterTests {
    @Test
    public void methodWithNoStatementsReturnsUnit() {
        val method = staticMethod("hello");
        val result = runMethod(method);
        
        assertEquals(UNIT, result);
    }
    
    @Test
    public void canReturnLiteralValue() {
        val method = staticMethod("hello")
            .statement(new ReturnNode(LiteralNode.literal("hello, world!")));
        val result = runMethod(method);
        
        assertEquals(new StringValue("hello, world!"), result);
    }
    
    @Test
    public void canPassValueToMethod() {
        val arg = new FormalArgumentNode(42, StringValue.REF, "x");
        val method = staticMethod("hello")
            .argument(arg)
            .statement(new ReturnNode(reference(arg)));
        val result = runMethod(method, new StringValue("hello, world!"));
        
        assertEquals(new StringValue("hello, world!"), result);
    }
    
    @Test
    public void errorIfWrongNumberOfArgumentsArePassed() {
        val method = staticMethod("hello");
        
        val exception = assertThrows(WrongNumberOfArguments.class,
            () -> runMethod(method, new StringValue("hello, world!")));
        
        assertEquals(new WrongNumberOfArguments(0, 1), exception);
    }
    
    @Test
    public void canReassignValueToArgument() {
        val arg = new FormalArgumentNode(42, StringValue.REF, "x");
        val method = staticMethod("hello")
            .argument(arg)
            .statement(new ExpressionStatementNode(new Assignment(reference(arg), LiteralNode.literal("[updated value]"))))
            .statement(new ReturnNode(reference(arg)));
        val result = runMethod(method, new StringValue("[initial value]"));
        
        assertEquals(new StringValue("[updated value]"), result);
    }
    
    @Test
    public void canDeclareVariable() {
        val localVariableDeclaration = localVariableDeclaration(
            42, StringValue.REF, "x", LiteralNode.literal("[initial value]"));
        val method = staticMethod("hello")
            .statement(localVariableDeclaration)
            .statement(new ReturnNode(reference(localVariableDeclaration)));
        val result = runMethod(method);
        
        assertEquals(new StringValue("[initial value]"), result);
    }
    
    @Test
    public void canDeclareVariableAndThenAssignValues() {
        val localVariableDeclaration = localVariableDeclaration(
            42, StringValue.REF, "x", LiteralNode.literal("[initial value]"));
        val method = staticMethod("hello")
            .statement(localVariableDeclaration)
            .statement(new ExpressionStatementNode(new Assignment(reference(localVariableDeclaration), LiteralNode.literal("[updated value]"))))
            .statement(new ReturnNode(reference(localVariableDeclaration)));
        val result = runMethod(method);
        
        assertEquals(new StringValue("[updated value]"), result);
    }
    
    @Test
    public void errorIfTryingToAssignToVariableNotInScope() {
        val localVariableDeclaration = localVariableDeclaration(
            42, StringValue.REF, "x", literal(""));
        val method = staticMethod("hello")
            .statement(new ExpressionStatementNode(new Assignment(reference(localVariableDeclaration), LiteralNode.literal("[updated value]"))));

        val exception = assertThrows(VariableNotInScope.class,
            () -> runMethod(method));
        
        assertEquals(new VariableNotInScope(42), exception);
    }

    private InterpreterValue runMethod(MethodNode.MethodNodeBuilder methodBuilder, InterpreterValue... arguments) {
        val method = methodBuilder.build();
        val className = "com.example.Program";
        val classNode = ClassNode.builder(className)
            .method(method)
            .build();
        val interpreter = new Interpreter(new MapBackedProject(ImmutableMap.of(
            className, ConcreteType.fromNode(classNode))));
        
        return interpreter.run(className, method.getName(), asList(arguments));
    }
}
