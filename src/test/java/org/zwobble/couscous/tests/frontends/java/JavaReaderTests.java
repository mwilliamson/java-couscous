package org.zwobble.couscous.tests.frontends.java;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.frontends.java.JavaReader;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AnnotationNode.annotation;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodCallNode.*;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.StaticMethodCallNode.boxBoolean;
import static org.zwobble.couscous.ast.StaticMethodCallNode.boxInt;
import static org.zwobble.couscous.ast.StaticMethodCallNode.same;
import static org.zwobble.couscous.ast.StaticMethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.StaticMethodCallNode.unboxInt;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;

public class JavaReaderTests {
    @Test
    public void canReadLiterals() {
        assertEquals(literal("hello"), readObjectExpression("\"hello\""));
        assertEquals(literal(true), readBooleanExpression("true"));
        assertEquals(literal(false), readBooleanExpression("false"));
        assertEquals(literal(42), readIntExpression("42"));
    }
    
    @Test
    public void canReadThisReference() {
        assertEquals(
            thisReference(TypeName.of("com.example.Example")),
            readExpressionInInstanceMethod("this"));
    }
    
    @Test
    public void canReadFieldDeclarations() {
        ClassNode classNode = readClass(
            "private String name;");
        
        assertEquals(
            asList(field("name", TypeName.of("java.lang.String"))),
            classNode.getFields());
    }
    
    @Test
    public void canReadExplicitFieldReference() {
        canReadFieldReference("this.name");
    }
    
    @Test
    public void canReadImplicitFieldReference() {
        canReadFieldReference("name");
    }
    
    private void canReadFieldReference(String expression) {
        ClassNode classNode = readClass(
            "private String name;" +
            "public String getName() {" +
            "    return " + expression + ";" +
            "}");
        
        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        assertEquals(
            fieldAccess(
                thisReference(TypeName.of("com.example.Example")),
                "name",
                StringValue.REF),
            returnNode.getValue());
    }
    
    @Test
    public void canReadInstanceMethodCalls() {
        assertEquals(
            methodCall(literal("hello"), "startsWith", asList(literal("h")), BooleanValue.REF),
            readBooleanExpression("\"hello\".startsWith(\"h\")"));
        
        List<StatementNode> statements = readStatements("int", "Object x = 1; return x.hashCode();");
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        ReturnNode returnNode = (ReturnNode) statements.get(1);
        assertEquals(
            methodCall(reference(declaration), "hashCode", asList(), IntegerValue.REF),
            returnNode.getValue());
    }
    
    @Test
    public void canReadImplicitInstanceMethodCalls() {
        ClassNode classNode = readClass(
            "public String loop() {" +
            "    return loop();" +
            "}");
        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        
        assertEquals(
            methodCall(
                ThisReferenceNode.thisReference(TypeName.of("com.example.Example")),
                "loop",
                asList(),
                StringValue.REF),
            returnNode.getValue());
    }
    
    @Test
    public void canReadStaticMethodCalls() {
        assertEquals(
            staticMethodCall(
                TypeName.of("java.lang.Integer"),
                "parseInt",
                asList(literal("42")),
                IntegerValue.REF),
            readIntExpression("Integer.parseInt(\"42\")"));
    }
    
    @Test
    public void canReadImplicitStaticMethodCalls() {
        ClassNode classNode = readClass(
            "public static String loop() {" +
            "    return loop();" +
            "}");
        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        
        assertEquals(
            staticMethodCall(
                TypeName.of("com.example.Example"),
                "loop",
                asList(),
                StringValue.REF),
            returnNode.getValue());
    }
    
    @Test
    public void canReadConstructorCalls() {
        assertEquals(
            constructorCall(TypeName.of("java.lang.String"), asList(literal("_"))),
            readObjectExpression("new String(\"_\")"));
    }
    
    @Test
    public void argumentIsBoxedIfNecessary() {
        StaticMethodCallNode expression = (StaticMethodCallNode) readObjectExpression(
            "java.util.Objects.toString(42)");
        assertEquals(
            boxInt(literal(42)),
            expression.getArguments().get(0));
    }
    
    @Test
    public void canUseOperatorsOnReferences() {
        assertEquals(
            same(
                constructorCall(TypeName.of("java.lang.Object"), emptyList()),
                constructorCall(TypeName.of("java.lang.Object"), emptyList())),
            readBooleanExpression("new Object() == new Object()"));
        
        assertEquals(
            not(same(
                constructorCall(TypeName.of("java.lang.Object"), emptyList()),
                constructorCall(TypeName.of("java.lang.Object"), emptyList()))),
            readBooleanExpression("new Object() != new Object()"));
    }
    
    @Test
    public void canUseOperatorsOnIntegers() {
        assertEquals(
            integerAdd(literal(1), literal(2)),
            readIntExpression("1 + 2"));
        assertEquals(
            integerSubtract(literal(1), literal(2)),
            readIntExpression("1 - 2"));
        assertEquals(
            integerMultiply(literal(1), literal(2)),
            readIntExpression("1 * 2"));
        assertEquals(
            integerDivide(literal(1), literal(2)),
            readIntExpression("1 / 2"));
        assertEquals(
            integerMod(literal(1), literal(2)),
            readIntExpression("1 % 2"));
        
        assertEquals(
            equal(literal(1), literal(2)),
            readBooleanExpression("1 == 2"));
        assertEquals(
            notEqual(literal(1), literal(2)),
            readBooleanExpression("1 != 2"));
        assertEquals(
            greaterThan(literal(1), literal(2)),
            readBooleanExpression("1 > 2"));
        assertEquals(
            greaterThanOrEqual(literal(1), literal(2)),
            readBooleanExpression("1 >= 2"));
        assertEquals(
            lessThan(literal(1), literal(2)),
            readBooleanExpression("1 < 2"));
        assertEquals(
            lessThanOrEqual(literal(1), literal(2)),
            readBooleanExpression("1 <= 2"));
    }

    @Test
    public void equalityOperatorUnboxesIfExactlyOneOperandIsPrimitive() {
        assertEquals(
            equal(
                literal(1),
                unboxInt(constructorCall(TypeName.of("java.lang.Integer"), asList(literal(1))))),
            readBooleanExpression("1 == new Integer(1)"));

        assertEquals(
            notEqual(
                literal(1),
                unboxInt(constructorCall(TypeName.of("java.lang.Integer"), asList(literal(1))))),
            readBooleanExpression("1 != new Integer(1)"));
    }

    @Test
    public void equalityOperatorDoesNotUnboxIfBothOperandsAreBoxed() {
        assertEquals(
            same(
                constructorCall(TypeName.of("java.lang.Integer"), asList(literal(1))),
                constructorCall(TypeName.of("java.lang.Integer"), asList(literal(2)))),
            readBooleanExpression("new Integer(1) == new Integer(2)"));

        assertEquals(
            not(same(
                constructorCall(TypeName.of("java.lang.Integer"), asList(literal(1))),
                constructorCall(TypeName.of("java.lang.Integer"), asList(literal(2))))),
            readBooleanExpression("new Integer(1) != new Integer(2)"));
    }

    @Test
    public void integerOperatorUnboxesWhenBothOperandsAreBoxed() {
        assertEquals(
            integerAdd(
                unboxInt(constructorCall(TypeName.of("java.lang.Integer"), asList(literal(1)))),
                unboxInt(constructorCall(TypeName.of("java.lang.Integer"), asList(literal(2))))),
            readIntExpression("new Integer(1) + new Integer(2)"));
    }

    @Test
    public void canReadTernaryConditionals() {
        assertEquals(
            ternaryConditional(literal(true), literal(1), literal(2)),
            readIntExpression("true ? 1 : 2"));
    }
    
    @Test
    public void canReadAssignments() {
        ClassNode classNode = readClass(
            "private String name;" +
            "public String getName() {" +
            "    return name = \"blah\";" +
            "}");
        
        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        assertEquals(
            assign(
                fieldAccess(
                    thisReference(TypeName.of("com.example.Example")),
                    "name",
                    StringValue.REF),
                literal("blah")),
            returnNode.getValue());
    }
    
    @Test
    public void valuesOfAssignmentsAreBoxedIfNecessary() {
        ClassNode classNode = readClass(
            "private Object value;" +
            "public Object getValue() {" +
            "    return value = 4;" +
            "}");
        
        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        assertEquals(
            assign(
                fieldAccess(
                    thisReference(TypeName.of("com.example.Example")),
                    "value",
                    ObjectValues.OBJECT),
                boxInt(literal(4))),
            returnNode.getValue());
    }
    
    @Test
    public void canDeclareAndReferenceLocalVariables() {
        List<StatementNode> statements = readStatements("int", "int x = 4; return x;");
        
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals("x", declaration.getName());
        assertEquals(IntegerValue.REF, declaration.getType());
        assertEquals(literal(4), declaration.getInitialValue());
        ReturnNode returnNode = (ReturnNode) statements.get(1);
        assertEquals(
            reference(declaration),
            returnNode.getValue());
    }
    
    @Test
    public void initialValueOfLocalVariableDeclarationsAreBoxedIfNecessary() {
        List<StatementNode> statements = readStatements("Object x = 4;");
        
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals(boxInt(literal(4)), declaration.getInitialValue());
    }
    
    @Test
    public void integersAreBoxedWhenTargetTypeIsObject() {
        List<StatementNode> statements = readStatements("Object x = 4;");
        
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals(boxInt(literal(4)), declaration.getInitialValue());
    }
    
    @Test
    public void booleansAreBoxedWhenTargetTypeIsObject() {
        List<StatementNode> statements = readStatements("Object x = true;");
        
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals(boxBoolean(literal(true)), declaration.getInitialValue());
    }
    
    @Test
    public void integersAreUnboxedIfTargetTypeIsPrimitive() {
        List<StatementNode> statements = readStatements("Integer x = 4; int y = x;");
        
        LocalVariableDeclarationNode boxedDeclaration = (LocalVariableDeclarationNode) statements.get(0);
        LocalVariableDeclarationNode unboxedDeclaration = (LocalVariableDeclarationNode) statements.get(1);
        assertEquals(unboxInt(reference(boxedDeclaration)), unboxedDeclaration.getInitialValue());
    }
    
    @Test
    public void booleansAreUnboxedIfTargetTypeIsPrimitive() {
        List<StatementNode> statements = readStatements("Boolean x = false; boolean y = x;");
        
        LocalVariableDeclarationNode boxedDeclaration = (LocalVariableDeclarationNode) statements.get(0);
        LocalVariableDeclarationNode unboxedDeclaration = (LocalVariableDeclarationNode) statements.get(1);
        assertEquals(StaticMethodCallNode.unboxBoolean(reference(boxedDeclaration)), unboxedDeclaration.getInitialValue());
    }
    
    @Test
    public void canDeclareAndReferenceArguments() {
        ClassNode classNode = readClass(
            "public int identity(int value) {" +
            "    return value;" +
            "}");
        
        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode argument = method.getArguments().get(0);
        assertEquals("value", argument.getName());
        assertEquals(TypeName.of("int"), argument.getType());
        assertEquals(1, method.getArguments().size());
        
        ReturnNode returnNode = (ReturnNode)method.getBody().get(0);
        assertEquals(reference(argument), returnNode.getValue());
    }

    @Test
    public void canReadPrefixIncrement() {
        ClassNode classNode = readClass(
            "public void go(int value) {" +
            "    ++value;" +
            "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode argument = method.getArguments().get(0);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get(0);
        assertEquals(
            assign(
                reference(argument),
                integerAdd(reference(argument), literal(1))),
            statement.getExpression());
    }

    @Test
    public void canReadCompoundAssignmentOperations() {
        canReadCompoundAssignmentOperation("+=", Operator.ADD);
        canReadCompoundAssignmentOperation("-=", Operator.SUBTRACT);
        canReadCompoundAssignmentOperation("*=", Operator.MULTIPLY);
        canReadCompoundAssignmentOperation("/=", Operator.DIVIDE);
        canReadCompoundAssignmentOperation("%=", Operator.MOD);
    }

    private void canReadCompoundAssignmentOperation(String symbol, Operator operator) {
        ClassNode classNode = readClass(
            "public void go(int value) {" +
            "    value " + symbol + " 2;" +
            "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode argument = method.getArguments().get(0);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get(0);
        assertEquals(
            assign(
                reference(argument),
                integerOperation(operator.getMethodName(), reference(argument), literal(2))),
            statement.getExpression());
    }

    @Test
    public void rightOperandOfCompoundAssignmentsIsUnboxedIfNecessary() {
        ClassNode classNode = readClass(
            "public void go(int left, Integer right) {" +
            "    left += right;" +
            "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode left = method.getArguments().get(0);
        FormalArgumentNode right = method.getArguments().get(1);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get(0);
        assertEquals(
            assign(
                reference(left),
                integerAdd(reference(left), unboxInt(reference(right)))),
            statement.getExpression());
    }

    @Test
    public void leftOperandOfCompoundAssignmentsIsUnboxedIfNecessary() {
        ClassNode classNode = readClass(
            "public void go(Integer left, int right) {" +
                "    left += right;" +
                "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode left = method.getArguments().get(0);
        FormalArgumentNode right = method.getArguments().get(1);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get(0);
        assertEquals(
            assign(
                reference(left),
                boxInt(integerAdd(unboxInt(reference(left)), reference(right)))),
            statement.getExpression());
    }

    @Test
    public void canReadPrefixDecrement() {
        ClassNode classNode = readClass(
            "public void go(int value) {" +
            "    --value;" +
            "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode argument = method.getArguments().get(0);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get(0);
        assertEquals(
            assign(
                reference(argument),
                integerSubtract(reference(argument), literal(1))),
            statement.getExpression());
    }
    
    @Test
    public void canReadExpressionStatements() {
        assertEquals(
            expressionStatement(
                staticMethodCall(
                    TypeName.of("java.lang.Integer"),
                    "parseInt",
                    asList(literal("42")),
                    IntegerValue.REF)),
            readStatement("Integer.parseInt(\"42\");"));
    }
    
    @Test
    public void canReadIfStatements() {
        assertEquals(
            ifStatement(
                literal(true),
                asList(returns(literal(1))),
                asList(returns(literal(2)))),
            readStatement("int", "if (true) { return 1; } else { return 2; }"));
    }

    @Test
    public void canReadWhileLoops() {
        assertEquals(
            whileLoop(
                literal(true),
                asList(returns(literal(1)))),
            readStatement("int", "while (true) { return 1; }"));
    }

    @Test
    public void canReadForLoops() {
        List<StatementNode> statements = readStatements("int", "for (int x = 0; true; ++x) { return 1; }");
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals(
            asList(
                localVariableDeclaration(declaration.getDeclaration(), literal(0)),
                whileLoop(
                    literal(true),
                    asList(
                        returns(literal(1)),
                        expressionStatement(assign(declaration, integerAdd(reference(declaration), literal(1))))))),
            statements);
    }
    
    @Test
    public void canDeclareConstructor() {
        ClassNode classNode = readClass(
            "private final String name;" +
            "public Example() {" +
            "    this.name = \"Flaws\";" +
            "}");
        
        ConstructorNode constructor = classNode.getConstructor();
        
        assertEquals(asList(), constructor.getArguments());
        
        assertEquals(
            asList(assignStatement(
                fieldAccess(
                    thisReference(TypeName.of("com.example.Example")),
                    "name",
                    StringValue.REF),
                literal("Flaws"))),
            constructor.getBody());
    }
    
    @Test
    public void canDeclareStaticMethodWithAnnotation() {
        ClassNode classNode = readClass(
            "@Deprecated public void doNothing() {}");
        
        MethodNode method = classNode.getMethods().get(0);
        assertEquals(
            asList(annotation(TypeName.of("java.lang.Deprecated"))),
            method.getAnnotations());
    }

    @Test
    public void returnValueHandlesBoxing() {
        ClassNode classNode = readClass(
            "public Integer one() { return 1; }");

        MethodNode method = classNode.getMethods().get(0);
        assertEquals(
            asList(returns(boxInt(literal(1)))),
            method.getBody());
    }

    private ExpressionNode readExpressionInInstanceMethod(String expressionSource) {
        String javaClass =
            "public Object main() {" +
            "    return " + expressionSource + ";" +
            "}";
        
        ClassNode classNode = readClass(javaClass);
        ReturnNode returnStatement = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        return returnStatement.getValue();
    }

    private ExpressionNode readObjectExpression(String expressionSource) {
        return readExpression("Object", expressionSource);
    }

    private ExpressionNode readBooleanExpression(String expressionSource) {
        return readExpression("boolean", expressionSource);
    }

    private ExpressionNode readIntExpression(String expressionSource) {
        return readExpression("int", expressionSource);
    }

    private ExpressionNode readExpression(String returnType, String expressionSource) {
        ReturnNode returnStatement = (ReturnNode) readStatement(returnType, "return " + expressionSource + ";");
        return returnStatement.getValue();
    }

    private StatementNode readStatement(String statementSource) {
        return readStatements(statementSource).get(0);
    }

    private StatementNode readStatement(String returnType, String statementSource) {
        return readStatements(returnType, statementSource).get(0);
    }

    private List<StatementNode> readStatements(String statementsSource) {
        return readStatements("Object", statementsSource);
    }

    private List<StatementNode> readStatements(String returnType, String statementsSource) {
        String javaClass =
            "public static " + returnType + " main() {" +
            statementsSource +
            "}";
        
        ClassNode classNode = readClass(javaClass);
        return classNode.getMethods().get(0).getBody();
    }

    private ClassNode readClass(String classBody) {
        String javaClass =
            "package com.example;" +
            "public class Example {" +
            classBody +
            "}";
        try {

            Path directoryPath = Files.createTempDirectory(null);
            Path sourcePath = directoryPath.resolve("com/example/Example.java");
            try {
                Files.createDirectories(directoryPath.resolve("com/example"));
                Files.write(sourcePath, asList(javaClass));
                
                JavaReader reader = new JavaReader();
                return reader.readClassFromFile(directoryPath, sourcePath);
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
