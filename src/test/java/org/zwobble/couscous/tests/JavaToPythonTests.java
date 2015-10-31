package org.zwobble.couscous.tests;

import java.nio.file.Files;
import java.util.List;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.frontends.java.JavaFrontend;
import org.zwobble.couscous.tests.backends.python.PythonMethodRunner;
import org.zwobble.couscous.values.PrimitiveValue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public class JavaToPythonTests {
    @Test
    public void canEvaluateLiterals() {
        assertEquals(value("hello"), evalExpression("\"hello\""));
        assertEquals(value(42), evalExpression("42"));
    }
    
    @Test
    public void canUseOperatorsOnPrimitives() {
        assertEquals(value(3), evalExpression("1 + 2"));
        assertEquals(value(false), evalExpression("1 > 2"));
    }
    
    private PrimitiveValue evalExpression(String expressionSource) {
        try {
            final java.lang.String javaClass = "package com.example;" + "public class Example {" + "    public static Object main() {" + "        return " + expressionSource + ";" + "    }" + "}";
            final java.nio.file.Path directoryPath = Files.createTempDirectory(null);
            try {
                Files.createDirectories(directoryPath.resolve("com/example"));
                Files.write(directoryPath.resolve("com/example/Example.java"), asList(javaClass));
                List<ClassNode> classNodes = JavaFrontend.readSourceDirectory(directoryPath);
                return new PythonMethodRunner().runMethod(classNodes, TypeName.of("com.example.Example"), "main", asList());
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (final java.lang.Throwable $ex) {
            throw new RuntimeException($ex);
        }
    }
}