package org.zwobble.couscous.tests.interpreter;

import java.util.Map;

import org.junit.Test;
import org.zwobble.couscous.Project;
import org.zwobble.couscous.ast.Assignment;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.interpreter.Interpreter;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;
import org.zwobble.couscous.values.InterpreterValue;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.MethodNode.staticMethod;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.util.ExtraAsserts.assertThrows;
import static org.zwobble.couscous.values.UnitValue.UNIT;

import lombok.val;

public class InterpreterTests {
    @Test
    public void canReturnLiteralValue() {
        val method = staticMethod("hello")
            .statement(new ReturnNode(LiteralNode.of("hello, world!")));
        val result = runMethod(method);
        
        assertEquals(new StringValue("hello, world!"), result);
    }
    
    @Test
    public void canPassValueToMethod() {
        val arg = new FormalArgumentNode(42, "x");
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
        val arg = new FormalArgumentNode(42, "x");
        val method = staticMethod("hello")
            .argument(arg)
            .statement(new ExpressionStatementNode(new Assignment(reference(arg), LiteralNode.of("[updated value]"))))
            .statement(new ReturnNode(reference(arg)));
        val result = runMethod(method, new StringValue("[initial value]"));
        
        assertEquals(new StringValue("[updated value]"), result);
    }
    
    @Test
    public void valueOfAssignmentExpressionIsNewValue() {
        val arg = new FormalArgumentNode(42, "x");
        val method = staticMethod("hello")
            .argument(arg)
            .statement(new ReturnNode(new Assignment(reference(arg), LiteralNode.of("[updated value]"))));
        val result = runMethod(method, new StringValue("[initial value]"));
        
        assertEquals(new StringValue("[updated value]"), result);
    }
    
    @Test
    public void methodWithNoStatementsReturnsUnit() {
        val method = staticMethod("hello");
        val result = runMethod(method);
        
        assertEquals(UNIT, result);
    }

    private InterpreterValue runMethod(MethodNode.MethodNodeBuilder methodBuilder, InterpreterValue... arguments) {
        val method = methodBuilder.build();
        val classNode = ClassNode.builder()
            .method(method)
            .build();
        val className = "com.example.Program";
        val interpreter = new Interpreter(new MapBackedProject(ImmutableMap.of(
            className, classNode)));
        
        return interpreter.run(className, method.getName(), asList(arguments));
    }

    private static class MapBackedProject implements Project {
        private Map<String, ClassNode> classes;

        public MapBackedProject(Map<String, ClassNode> classes) {
            this.classes = classes;
        }
        
        @Override
        public ClassNode findClass(String name) {
            return classes.get(name);
        }
        
    }
}
