package org.zwobble.couscous.tests.interpreter;

import java.util.List;

import org.junit.Test;
import org.zwobble.couscous.MapBackedProject;
import org.zwobble.couscous.ast.Assignment;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.interpreter.Interpreter;
import org.zwobble.couscous.interpreter.UnboundVariable;
import org.zwobble.couscous.interpreter.UnexpectedValueType;
import org.zwobble.couscous.interpreter.VariableNotInScope;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import org.zwobble.couscous.interpreter.values.IntegerValue;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.StringValue;
import org.zwobble.couscous.tests.BackendTests;
import org.zwobble.couscous.tests.MethodRunner;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodNode.staticMethod;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.util.ExtraAsserts.assertThrows;

import lombok.val;

public class InterpreterTests extends BackendTests {
    @Test
    public void canPassValueToMethod() {
        val arg = formalArg(var(42, "x", StringValue.REF));
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
        val arg = formalArg(var(42, "x", StringValue.REF));
        val method = staticMethod("hello")
            .argument(arg)
            .statement(new ExpressionStatementNode(new Assignment(reference(arg), LiteralNode.literal("[updated value]"))))
            .statement(new ReturnNode(reference(arg)));
        val result = runMethod(method, new StringValue("[initial value]"));
        
        assertEquals(new StringValue("[updated value]"), result);
    }
    
    @Test
    public void errorIfTryingToAssignValueOfWrongTypeToVariable() {
        val arg = formalArg(var(42, "x", StringValue.REF));
        val method = staticMethod("hello")
            .argument(arg)
            .statement(new ExpressionStatementNode(new Assignment(reference(arg), literal(0))));

        val exception = assertThrows(UnexpectedValueType.class,
            () -> runMethod(method, new StringValue("")));
        
        assertEquals(new UnexpectedValueType(StringValue.REF, IntegerValue.REF), exception);
    }
    
    @Test
    public void canDeclareVariable() {
        val localVariableDeclaration = localVariableDeclaration(
            42, "x", StringValue.REF, LiteralNode.literal("[initial value]"));
        val method = staticMethod("hello")
            .statement(localVariableDeclaration)
            .statement(new ReturnNode(reference(localVariableDeclaration)));
        val result = runMethod(method);
        
        assertEquals(new StringValue("[initial value]"), result);
    }
    
    @Test
    public void canDeclareVariableAndThenAssignValues() {
        val localVariableDeclaration = localVariableDeclaration(
            42, "x", StringValue.REF, LiteralNode.literal("[initial value]"));
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
            42, "x", StringValue.REF, literal(""));
        val method = staticMethod("hello")
            .statement(new ExpressionStatementNode(new Assignment(reference(localVariableDeclaration), LiteralNode.literal("[updated value]"))));

        val exception = assertThrows(VariableNotInScope.class,
            () -> runMethod(method));
        
        assertEquals(new VariableNotInScope(42), exception);
    }
    
    @Test
    public void errorIfTryingToGetValueOfVariableNotInScope() {
        val localVariableDeclaration = localVariableDeclaration(
            42, "x", StringValue.REF, literal(""));
        val method = staticMethod("hello")
            .statement(new ReturnNode(reference(localVariableDeclaration)));

        val exception = assertThrows(VariableNotInScope.class,
            () -> runMethod(method));
        
        assertEquals(new VariableNotInScope(42), exception);
    }
    
    @Test
    public void errorIfTryingToGetValueOfUnboundVariable() {
        val localVariableDeclaration = localVariableDeclaration(
            42, "x", StringValue.REF, literal(""));
        val method = staticMethod("hello")
            .statement(new ReturnNode(reference(localVariableDeclaration)))
            .statement(localVariableDeclaration);

        val exception = assertThrows(UnboundVariable.class,
            () -> runMethod(method));
        
        assertEquals(new UnboundVariable(42), exception);
    }

    @Override
    protected MethodRunner buildMethodRunner() {
        return new MethodRunner() {
            @Override
            public InterpreterValue runMethod(
                    ClassNode classNode,
                    String methodName,
                    List<InterpreterValue> arguments) {
                val interpreter = new Interpreter(new MapBackedProject(ImmutableMap.of(
                    classNode.getName(), ConcreteType.fromNode(classNode))));
                
                return interpreter.run(classNode.getName(), methodName, arguments);
            }
        };
    }
}
