package org.zwobble.couscous.tests.backends.python;

import org.junit.Test;
import org.zwobble.couscous.backends.python.ast.PythonClassNode;
import org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.backends.python.PythonSerializer.serialize;
import static org.zwobble.couscous.backends.python.ast.PythonBooleanLiteralNode.pythonBooleanLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode.pythonIntegerLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonModuleNode.pythonModule;
import static org.zwobble.couscous.backends.python.ast.PythonPassNode.PASS;
import static org.zwobble.couscous.backends.python.ast.PythonReturnNode.pythonReturn;
import static org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode.pythonStringLiteral;

import lombok.val;

public class PythonSerializerTests {
    @Test
    public void integersAreNotBoxed() {
        val output = serialize(pythonIntegerLiteral(42));
        assertEquals("42", output);
    }
    
    @Test
    public void stringsAreNotBoxed() {
        // TODO: escaping
        val output = serialize(pythonStringLiteral("blah"));
        assertEquals("\"blah\"", output);
    }
    
    @Test
    public void booleansAreNotBoxed() {
        assertEquals("True", serialize(pythonBooleanLiteral(true)));
        assertEquals("False", serialize(pythonBooleanLiteral(false)));
    }
    
    @Test
    public void returnKeywordIsUsedForReturns() {
        val output = serialize(pythonReturn(pythonIntegerLiteral(42)));
        assertEquals("return 42\n", output);
    }
    
    @Test
    public void passStatementIsSerializedAsPassKeyword() {
        val output = serialize(PASS);
        assertEquals("pass\n", output);
    }
    
    @Test
    public void emptyFunctionHasPassStatement() {
        val function = PythonFunctionDefinitionNode.builder("empty").build();
        val output = serialize(function);
        assertEquals("def empty():\n    pass\n", output);
    }
    
    @Test
    public void functionArgumentsAreSerializedWithName() {
        val function = PythonFunctionDefinitionNode.builder("empty")
            .argument("one")
            .argument("two")
            .build();
        val output = serialize(function);
        assertEquals("def empty(one, two):\n    pass\n", output);
    }
    
    @Test
    public void emptyClassHasPassStatement() {
        val classNode = PythonClassNode.builder("Empty")
            .build();
        val output = serialize(classNode);
        assertEquals("class Empty(object):\n    pass\n", output);
    }
    
    @Test
    public void moduleIsSerializedStatements() {
        val classNode = pythonModule(asList(PASS));
        val output = serialize(classNode);
        assertEquals("pass\n", output);
    }
    
    @Test
    public void bodiesOfBlocksAreIndented() {
        val classNode = PythonClassNode.builder("Foo")
            .statement(PythonFunctionDefinitionNode.builder("one").build())
            .statement(PythonFunctionDefinitionNode.builder("two").build())
            .build();
        val output = serialize(classNode);
        val expectedOutput =
            "class Foo(object):\n" +
            "    def one():\n" +
            "        pass\n" +
            "    def two():\n" +
            "        pass\n";
        assertEquals(expectedOutput, output);
    }
}
