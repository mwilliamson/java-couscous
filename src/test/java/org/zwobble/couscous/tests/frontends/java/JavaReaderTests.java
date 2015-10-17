package org.zwobble.couscous.tests.frontends.java;

import java.nio.file.Files;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.ThisReferenceNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.frontends.java.JavaReader;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.StaticMethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;

import lombok.SneakyThrows;
import lombok.val;

public class JavaReaderTests {
    @Test
    public void canReadLiterals() {
        assertEquals(literal("hello"), readExpression("\"hello\""));
        assertEquals(literal(true), readExpression("true"));
        assertEquals(literal(false), readExpression("false"));
        assertEquals(literal(42), readExpression("42"));
    }
    
    @Test
    public void canReadThisReference() {
        assertEquals(
            thisReference(TypeName.of("com.example.Example")),
            readExpressionInInstanceMethod("this"));
    }
    
    @Test
    public void canReadFieldDeclarations() {
        val classNode = readClass(
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
        val classNode = readClass(
            "private String name;" +
            "public String getName() {" +
            "    return " + expression + ";" +
            "}");
        
        val returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
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
            readExpression("\"hello\".startsWith(\"h\")"));
    }
    
    @Test
    public void canReadImplicitInstanceMethodCalls() {
        val classNode = readClass(
            "public String loop() {" +
            "    return loop();" +
            "}");
        val returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        
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
            staticMethodCall(TypeName.of("java.lang.Integer"), "parseInt", asList(literal("42"))),
            readExpression("Integer.parseInt(\"42\")"));
    }
    
    @Test
    public void canReadImplicitStaticMethodCalls() {
        val classNode = readClass(
            "public static String loop() {" +
            "    return loop();" +
            "}");
        val returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        
        assertEquals(
            staticMethodCall(
                TypeName.of("com.example.Example"),
                "loop",
                asList()),
            returnNode.getValue());
    }
    
    @Test
    public void canReadConstructorCalls() {
        assertEquals(
            constructorCall(TypeName.of("java.lang.String"), asList(literal("_"), literal(42))),
            readExpression("new String(\"_\", 42)"));
    }
    
    @Test
    public void canReadTernaryConditionals() {
        assertEquals(
            new TernaryConditionalNode(literal(true), literal(1), literal(2)),
            readExpression("true ? 1 : 2"));
    }
    
    @Test
    public void canReadAssignments() {
        val classNode = readClass(
            "private String name;" +
            "public String getName() {" +
            "    return name = \"blah\";" +
            "}");
        
        val returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
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
    public void canReadExpressionStatements() {
        assertEquals(
            new ExpressionStatementNode(
                staticMethodCall(
                    TypeName.of("java.lang.Integer"),
                    "parseInt",
                    asList(literal("42")))),
            readStatement("Integer.parseInt(\"42\");"));
    }

    private ExpressionNode readExpressionInInstanceMethod(String expressionSource) {
        val javaClass =
            "public Object main() {" +
            "    return " + expressionSource + ";" +
            "}";
        
        val classNode = readClass(javaClass);
        val returnStatement = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        return returnStatement.getValue();
    }

    private ExpressionNode readExpression(String expressionSource) {
        val returnStatement = (ReturnNode) readStatement("return " + expressionSource + ";");
        return returnStatement.getValue();
    }

    private StatementNode readStatement(String statementSource) {
        val javaClass =
            "public static Object main() {" +
            statementSource +
            "}";
        
        val classNode = readClass(javaClass);
        return classNode.getMethods().get(0).getBody().get(0);
    }

    @SneakyThrows
    private ClassNode readClass(String classBody) {
        val javaClass =
            "package com.example;" +
            "public class Example {" +
            classBody +
            "}";
        
        val directoryPath = Files.createTempDirectory(null);
        val sourcePath = directoryPath.resolve("com/example/Example.java");
        try {
            Files.createDirectories(directoryPath.resolve("com/example"));
            Files.write(sourcePath, asList(javaClass));
            
            val reader = new JavaReader();
            return reader.readClassFromFile(directoryPath, sourcePath);
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }
}
