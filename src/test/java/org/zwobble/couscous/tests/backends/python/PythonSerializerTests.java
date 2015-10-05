package org.zwobble.couscous.tests.backends.python;

import org.junit.Test;
import org.zwobble.couscous.backends.python.ast.PythonClassNode;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.backends.python.PythonSerializer.serialize;
import static org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode.pythonFunctionDefinition;
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
        val output = serialize(pythonFunctionDefinition("empty", asList()));
        assertEquals("def empty():\n    pass\n", output);
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
            .statement(pythonFunctionDefinition("one", asList()))
            .statement(pythonFunctionDefinition("two", asList()))
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
