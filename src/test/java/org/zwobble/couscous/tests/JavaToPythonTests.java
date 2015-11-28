package org.zwobble.couscous.tests;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.couscous.CouscousCompiler;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.PythonBackend;
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
        assertEquals(value(true), evalExpression("1 == 1"));
        assertEquals(value(false), evalExpression("1 != 1"));
    }
    
    @Test
    public void equalityOnReferenceTypesChecksForIdentity() {
        assertEquals(value(false), evalExpression("new Object() == new Object()"));
        assertEquals(value(true), exec("Object x = new Object(); return x == x;"));
    }
    
    @Test
    public void integersCanBeAssignedToObjectVariable() {
        assertEquals(value(false), exec("Object x = 1; return x.equals(new Object());"));
    }
    
    @Test
    public void recursiveFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "recursive-factorial",
                TypeName.of("com.example.RecursiveFactorial"),
                "factorial",
                asList(value(6))));
    }

    @Test
    public void whileFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "while-factorial",
                TypeName.of("com.example.WhileFactorial"),
                "factorial",
                asList(value(6))));
    }

    @Test
    public void forFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "for-factorial",
                TypeName.of("com.example.ForFactorial"),
                "factorial",
                asList(value(6))));
    }

    @Test
    public void anonymousClassesWithoutCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "anonymous-class",
                TypeName.of("com.example.AnonymousClass"),
                "value",
                asList()));
    }

    @Test
    public void anonymousClassesWithCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "anonymous-class-capture",
                TypeName.of("com.example.AnonymousClass"),
                "value",
                asList(value(42))));
    }

    @Test
    public void lambdasWithoutCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda",
                TypeName.of("com.example.Lambda"),
                "value",
                asList()));
    }

    @Test
    public void lambdasWithCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda-capture",
                TypeName.of("com.example.Lambda"),
                "value",
                asList(value(2))));
    }
    
    private PrimitiveValue execTestProgram(
            String directoryName,
            TypeName type,
            String methodName,
            List<PrimitiveValue> arguments) throws IOException, InterruptedException {
        
        Path path = pathForResource(
            "/java/" + directoryName + "/" + type.getQualifiedName().replace(".", "/") + ".java");
        
        return execProgram(
            directoryName(path, type.getQualifiedName().split(".").length),
            type,
            methodName,
            arguments);
    }
    
    private PrimitiveValue execProgram(
            Path directory,
            TypeName type,
            String methodName,
            List<PrimitiveValue> arguments) throws IOException, InterruptedException {
        
        Path directoryPath = Files.createTempDirectory(null);
        try {
            CouscousCompiler compiler = new CouscousCompiler(
                new JavaFrontend(),
                new PythonBackend(directoryPath, "couscous"));
            compiler.compileDirectory(directory);
            return PythonMethodRunner.runFunction(directoryPath, type, methodName, arguments);
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }
    
    private Path directoryName(Path path, int length) {
        for (int i = 0; i < length; i++) {
            path = path.getParent();
        }
        return path;
    }

    private PrimitiveValue exec(String source) {
        try {
            String javaClass =
                "package com.example;" +
                "public class Example {" +
                "    public static Object main() {" + source + "}" +
                "}";
            Path directoryPath = Files.createTempDirectory(null);
            try {
                Files.createDirectories(directoryPath.resolve("com/example"));
                Files.write(directoryPath.resolve("com/example/Example.java"), asList(javaClass));
                return execProgram(
                    directoryPath,
                    TypeName.of("com.example.Example"),
                    "main",
                    asList());
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (final java.lang.Throwable $ex) {
            throw new RuntimeException($ex);
        }
    }
    
    private PrimitiveValue evalExpression(String expressionSource) {
        return exec("return " + expressionSource + ";");
    }
    
    private static Path pathForResource(String name) {
        try {
            URI uri = JavaToPythonTests.class.getResource(name).toURI();
            return new File(uri).toPath();
        } catch (URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }
}