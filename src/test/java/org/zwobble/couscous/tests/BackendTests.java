package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.StringValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodNode.staticMethod;
import static org.zwobble.couscous.interpreter.values.UnitValue.UNIT;

import lombok.val;

public abstract class BackendTests {
    @Test
    public void methodWithNoStatementsReturnsUnit() {
        val method = staticMethod("hello");
        val result = runMethod(method);
        
        assertEquals(UNIT, result);
    }
    
    @Test
    public void canReturnLiteralValue() {
        val method = staticMethod("hello")
            .statement(new ReturnNode(literal("hello, world!")));
        val result = runMethod(method);
        
        assertEquals(new StringValue("hello, world!"), result);
    }

    protected InterpreterValue runMethod(MethodNode.MethodNodeBuilder methodBuilder, InterpreterValue... arguments) {
        val method = methodBuilder.build();
        val className = "com.example.Program";
        val classNode = ClassNode.builder(className)
            .method(method)
            .build();
        
        val runner = buildMethodRunner();
        return runner.runMethod(classNode, method.getName(), asList(arguments));
    }
    
    protected abstract MethodRunner buildMethodRunner();
        
}
