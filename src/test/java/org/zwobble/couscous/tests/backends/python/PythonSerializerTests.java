package org.zwobble.couscous.tests.backends.python;

import org.junit.Test;
import org.zwobble.couscous.backends.python.ast.PythonClassNode;
import org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode;
import org.zwobble.couscous.backends.python.ast.PythonModuleNode;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.backends.python.PythonSerializer.serialize;
import static org.zwobble.couscous.backends.python.ast.PythonArrayNode.pythonArray;
import static org.zwobble.couscous.backends.python.ast.PythonAssignmentNode.pythonAssignment;
import static org.zwobble.couscous.backends.python.ast.PythonAttributeAccessNode.pythonAttributeAccess;
import static org.zwobble.couscous.backends.python.ast.PythonBinaryOperation.pythonAnd;
import static org.zwobble.couscous.backends.python.ast.PythonBinaryOperation.pythonIs;
import static org.zwobble.couscous.backends.python.ast.PythonBooleanLiteralNode.pythonBooleanLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonCallNode.pythonCall;
import static org.zwobble.couscous.backends.python.ast.PythonConditionalExpressionNode.pythonConditionalExpression;
import static org.zwobble.couscous.backends.python.ast.PythonGetSliceNode.pythonGetSlice;
import static org.zwobble.couscous.backends.python.ast.PythonIfStatementNode.pythonIfStatement;
import static org.zwobble.couscous.backends.python.ast.PythonImportAliasNode.pythonImportAlias;
import static org.zwobble.couscous.backends.python.ast.PythonImportNode.pythonImport;
import static org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode.pythonIntegerLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonModuleNode.pythonModule;
import static org.zwobble.couscous.backends.python.ast.PythonNotNode.pythonNot;
import static org.zwobble.couscous.backends.python.ast.PythonPassNode.PASS;
import static org.zwobble.couscous.backends.python.ast.PythonReturnNode.pythonReturn;
import static org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode.pythonStringLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode.pythonVariableReference;
import static org.zwobble.couscous.backends.python.ast.PythonWhileNode.pythonWhile;
import static org.zwobble.couscous.util.ExtraLists.list;

public class PythonSerializerTests {
    @Test
    public void integersAreNotBoxed() {
        String output = serialize(pythonIntegerLiteral(42));
        assertEquals("42", output);
    }
    
    @Test
    public void stringsAreNotBoxed() {
        String output = serialize(pythonStringLiteral("blah"));
        assertEquals("\"blah\"", output);
    }

    @Test
    public void stringsAreEscaped() {
        String output = serialize(pythonStringLiteral("\"\n\r\t\\"));
        assertEquals("\"\\\"\\n\\r\\t\\\\\"", output);
    }
    
    @Test
    public void booleansAreNotBoxed() {
        assertEquals("True", serialize(pythonBooleanLiteral(true)));
        assertEquals("False", serialize(pythonBooleanLiteral(false)));
    }
    
    @Test
    public void variableReferenceIsSerializedAsIdentifier() {
        assertEquals("blah", serialize(pythonVariableReference("blah")));
    }

    @Test
    public void elementsOfArrayAreSerialised() {
        assertEquals("[]", serialize(pythonArray(list())));
        assertEquals("[one]", serialize(pythonArray(list(pythonVariableReference("one")))));
        assertEquals(
            "[one, two]",
            serialize(pythonArray(list(
                pythonVariableReference("one"),
                pythonVariableReference("two")))));
    }
    
    @Test
    public void conditionalExpressionIsSerializedUsingSubExpressions() {
        assertEquals("1 if True else 2", serialize(pythonConditionalExpression(pythonBooleanLiteral(true), pythonIntegerLiteral(1), pythonIntegerLiteral(2))));
    }
    
    @Test
    public void attributeAccessIsSerializedUsingSubExpression() {
        assertEquals("x.y", serialize(pythonAttributeAccess(pythonVariableReference("x"), "y")));
    }
    
    @Test
    public void callIsSerializedUsingSubExpression() {
        assertEquals("f(x, y)", serialize(pythonCall(pythonVariableReference("f"), list(pythonVariableReference("x"), pythonVariableReference("y")))));
    }
    
    @Test
    public void getSliceIsSerializedUsingSubExpression() {
        assertEquals("x[y:z]", serialize(pythonGetSlice(pythonVariableReference("x"), list(pythonVariableReference("y"), pythonVariableReference("z")))));
    }
    
    @Test
    public void notOperationIsSerializedUsingSubExpression() {
        assertEquals("not x", serialize(pythonNot(pythonVariableReference("x"))));
    }
    
    @Test
    public void binaryOperationIsSerializedUsingSubExpressions() {
        assertEquals(
            "x is y",
            serialize(pythonIs(pythonVariableReference("x"), pythonVariableReference("y"))));
    }

    @Test
    public void operationsWithSameOrLowerPrecedenceIsParenthesised() {
        assertEquals(
            "not (x and y)",
            serialize(pythonNot(pythonAnd(pythonVariableReference("x"), pythonVariableReference("y")))));

        assertEquals(
            "not x and y",
            serialize(pythonAnd(pythonNot(pythonVariableReference("x")), pythonVariableReference("y"))));
    }
    
    @Test
    public void returnKeywordIsUsedForReturns() {
        String output = serialize(pythonReturn(pythonIntegerLiteral(42)));
        assertEquals("return 42\n", output);
    }
    
    @Test
    public void passStatementIsSerializedAsPassKeyword() {
        final java.lang.Object output = serialize(PASS);
        assertEquals("pass\n", output);
    }
    
    @Test
    public void emptyFunctionHasPassStatement() {
        PythonFunctionDefinitionNode function = PythonFunctionDefinitionNode.builder("empty").build();
        String output = serialize(function);
        assertEquals("def empty():\n    pass\n", output);
    }
    
    @Test
    public void functionArgumentsAreSerializedWithName() {
        PythonFunctionDefinitionNode function = PythonFunctionDefinitionNode.builder("empty")
            .argument("one")
            .argument("two")
            .build();
        String output = serialize(function);
        assertEquals("def empty(one, two):\n    pass\n", output);
    }
    
    @Test
    public void emptyClassHasPassStatement() {
        PythonClassNode classNode = PythonClassNode.builder("Empty").build();
        String output = serialize(classNode);
        assertEquals("class Empty(object):\n    pass\n", output);
    }
    
    @Test
    public void bodiesOfBlocksAreIndented() {
        PythonClassNode classNode = PythonClassNode.builder("Foo")
            .statement(PythonFunctionDefinitionNode.builder("one").build())
            .statement(PythonFunctionDefinitionNode.builder("two").build())
            .build();
        String output = serialize(classNode);
        String expectedOutput = "class Foo(object):\n    def one():\n        pass\n    def two():\n        pass\n";
        assertEquals(expectedOutput, output);
    }
    
    @Test
    public void assignmentIsSerializedWithEqualsSymbolSeparatingTargetAndValue() {
        String output = serialize(pythonAssignment(
            pythonVariableReference("x"),
            pythonIntegerLiteral(42)));
        assertEquals("x = 42\n", output);
    }
    
    @Test
    public void importIsSerializedUsingImportKeyword() {
        String output = serialize(pythonImport(
            "com.example",
            list(pythonImportAlias("Program"))));
        assertEquals("from com.example import Program\n", output);
    }
    
    @Test
    public void importAliasesAreSeparatedByCommas() {
        String output = serialize(pythonImport(
            "com.example",
            list(pythonImportAlias("Program"),pythonImportAlias("Runner"))));
        assertEquals("from com.example import Program, Runner\n", output);
    }
    
    @Test
    public void ifStatementIsSerializedWithIfKeyword() {
        String output = serialize(pythonIfStatement(
            pythonBooleanLiteral(true),
            list(pythonReturn(pythonIntegerLiteral(1))),
            list(pythonReturn(pythonIntegerLiteral(2)))));
        assertEquals("if True:\n    return 1\nelse:\n    return 2\n", output);
    }

    @Test
    public void whileIsSerializedWithWhileKeyword() {
        String output = serialize(pythonWhile(
            pythonBooleanLiteral(true),
            list(pythonReturn(pythonIntegerLiteral(1)))));
        assertEquals("while True:\n    return 1\n", output);
    }
    
    @Test
    public void moduleIsSerializedStatements() {
        PythonModuleNode classNode = pythonModule(list(PASS));
        String output = serialize(classNode);
        assertEquals("pass\n", output);
    }
}