package org.zwobble.couscous.tests.frontends.java;

import java.nio.file.Files;

import org.junit.Test;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.frontends.java.JavaReader;
import org.zwobble.couscous.values.BooleanValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
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
    public void canReadInstanceMethodCalls() {
        assertEquals(
            methodCall(literal("hello"), "startsWith", asList(literal("h")), BooleanValue.REF),
            readExpression("\"hello\".startsWith(\"h\")"));
    }

    @SneakyThrows
    private ExpressionNode readExpression(String expressionSource) {
        val javaClass =
            "package com.example;" +
            "public class Example {" +
            "    public static Object main() {" +
            "        return " + expressionSource + ";" +
            "    }" +
            "}";
        
        val directoryPath = Files.createTempDirectory(null);
        val sourcePath = directoryPath.resolve("com/example/Example.java");
        try {
            Files.createDirectories(directoryPath.resolve("com/example"));
            Files.write(sourcePath, asList(javaClass));
            
            val reader = new JavaReader();
            val classNode = reader.readClassFromFile(directoryPath, sourcePath);
            val returnStatement = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
            return returnStatement.getValue();
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }
}
