package org.zwobble.couscous.tests;

import java.nio.file.Files;

import org.junit.Test;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.frontends.java.JavaFrontend;
import org.zwobble.couscous.tests.backends.python.PythonMethodRunner;
import org.zwobble.couscous.values.PrimitiveValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.values.PrimitiveValues.value;

import lombok.SneakyThrows;
import lombok.val;

public class JavaToPythonTests {
    @Test
    public void canEvaluateLiterals() {
        assertEquals(value("hello"), evalExpression("\"hello\""));
        assertEquals(value(true), evalExpression("true"));
        assertEquals(value(false), evalExpression("false"));
        assertEquals(value(42), evalExpression("42"));
    }

    @SneakyThrows
    private PrimitiveValue evalExpression(String expressionSource) {
        val javaClass =
            "package com.example;" +
            "public class Example {" +
            "    public static Object main() {" +
            "        return " + expressionSource + ";" +
            "    }" +
            "}";

        val directoryPath = Files.createTempDirectory(null);
        try {
            Files.createDirectories(directoryPath.resolve("com/example"));
            Files.write(
                directoryPath.resolve("com/example/Example.java"),
                asList(javaClass));
            
            val classNodes = JavaFrontend.readSourceDirectory(directoryPath);
            
            return new PythonMethodRunner().runMethod(
                classNodes,
                TypeName.of("com.example.Example"),
                "main",
                asList());
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }
}
