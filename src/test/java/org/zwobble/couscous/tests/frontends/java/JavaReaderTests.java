package org.zwobble.couscous.tests.frontends.java;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ConstructorNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.ThisReferenceNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.frontends.java.JavaReader;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;
import org.zwobble.couscous.values.StringValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AnnotationNode.annotation;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.MethodCallNode.not;
import static org.zwobble.couscous.ast.StaticMethodCallNode.boxInt;
import static org.zwobble.couscous.ast.StaticMethodCallNode.same;
import static org.zwobble.couscous.ast.StaticMethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;

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
            readExpression("\"hello\".startsWith(\"h\")"));
        
        List<StatementNode> statements = readStatements("Object x = 1; return x.hashCode();");
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
            readExpression("Integer.parseInt(\"42\")"));
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
            readExpression("new String(\"_\")"));
    }
    
    @Test
    public void argumentIsBoxedIfNecessary() {
        StaticMethodCallNode expression = (StaticMethodCallNode)readExpression(
            "java.util.Objects.toString(42)");
        assertEquals(
            boxInt(literal(42)),
            expression.getArguments().get(0));
    }
    
    @Test
    public void canUseOperatorsOnReferences() {
        assertEquals(
            same(
                constructorCall(TypeName.of("java.lang.Object"), Collections.emptyList()),
                constructorCall(TypeName.of("java.lang.Object"), Collections.emptyList())),
            readExpression("new Object() == new Object()"));
        
        assertEquals(
            not(same(
                constructorCall(TypeName.of("java.lang.Object"), Collections.emptyList()),
                constructorCall(TypeName.of("java.lang.Object"), Collections.emptyList()))),
            readExpression("new Object() != new Object()"));
    }
    
    @Test
    public void canUseOperatorsOnIntegers() {
        assertEquals(
            methodCall(literal(1), "add", asList(literal(2)), IntegerValue.REF),
            readExpression("1 + 2"));
        assertEquals(
            methodCall(literal(1), "subtract", asList(literal(2)), IntegerValue.REF),
            readExpression("1 - 2"));
        assertEquals(
            methodCall(literal(1), "multiply", asList(literal(2)), IntegerValue.REF),
            readExpression("1 * 2"));
        assertEquals(
            methodCall(literal(1), "divide", asList(literal(2)), IntegerValue.REF),
            readExpression("1 / 2"));
        assertEquals(
            methodCall(literal(1), "mod", asList(literal(2)), IntegerValue.REF),
            readExpression("1 % 2"));
        
        assertEquals(
            methodCall(literal(1), "equals", asList(literal(2)), BooleanValue.REF),
            readExpression("1 == 2"));
        assertEquals(
            not(methodCall(literal(1), "equals", asList(literal(2)), BooleanValue.REF)),
            readExpression("1 != 2"));
        assertEquals(
            methodCall(literal(1), "greaterThan", asList(literal(2)), BooleanValue.REF),
            readExpression("1 > 2"));
        assertEquals(
            methodCall(literal(1), "greaterThanOrEqual", asList(literal(2)), BooleanValue.REF),
            readExpression("1 >= 2"));
        assertEquals(
            methodCall(literal(1), "lessThan", asList(literal(2)), BooleanValue.REF),
            readExpression("1 < 2"));
        assertEquals(
            methodCall(literal(1), "lessThanOrEqual", asList(literal(2)), BooleanValue.REF),
            readExpression("1 <= 2"));
    }
    
    @Test
    public void canReadTernaryConditionals() {
        assertEquals(
            ternaryConditional(literal(true), literal(1), literal(2)),
            readExpression("true ? 1 : 2"));
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
    public void integersAreBoxedIfAssignedToObjectVariable() {
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
        List<StatementNode> statements = readStatements("int x = 4; return x;");
        
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
    public void integersAreBoxedIfDeclaredAsObjectVariable() {
        List<StatementNode> statements = readStatements("Object x = 4;");
        
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals(StaticMethodCallNode.boxInt(literal(4)), declaration.getInitialValue());
    }
    
    @Test
    public void canDeclareAndReferenceArguments() {
        ClassNode classNode = readClass(
            "public String identity(int value) {" +
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

    private ExpressionNode readExpressionInInstanceMethod(String expressionSource) {
        String javaClass =
            "public Object main() {" +
            "    return " + expressionSource + ";" +
            "}";
        
        ClassNode classNode = readClass(javaClass);
        ReturnNode returnStatement = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        return returnStatement.getValue();
    }

    private ExpressionNode readExpression(String expressionSource) {
        ReturnNode returnStatement = (ReturnNode) readStatement("return " + expressionSource + ";");
        return returnStatement.getValue();
    }

    private StatementNode readStatement(String statementSource) {
        return readStatements(statementSource).get(0);
    }

    private List<StatementNode> readStatements(String statementsSource) {
        String javaClass =
            "public static Object main() {" +
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
