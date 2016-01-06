package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.values.PrimitiveValue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public abstract class CompilerTests {
    @Test
    public void canEvaluateLiterals() {
        assertEquals(value("hello"), evalObjectExpression("\"hello\""));
        assertEquals(value(42), evalIntExpression("42"));
    }

    @Test
    public void canUseOperatorsOnPrimitives() {
        assertEquals(value(3), evalIntExpression("1 + 2"));
        assertEquals(value(false), evalBooleanExpression("1 > 2"));
        assertEquals(value(true), evalBooleanExpression("1 == 1"));
        assertEquals(value(false), evalBooleanExpression("1 != 1"));
        assertEquals(value(true), evalBooleanExpression("!false"));
    }

    @Test
    public void equalityOnReferenceTypesChecksForIdentity() {
        assertEquals(value(false), evalBooleanExpression("new Object() == new Object()"));
        assertEquals(value(true), exec("boolean", "Object x = new Object(); return x == x;"));
    }

    @Test
    public void integersCanBeAssignedToObjectVariable() {
        assertEquals(value(false), exec("boolean", "Object x = 1; return x.equals(new Object());"));
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

    @Test
    public void lambdasWithThisCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda-this-capture",
                TypeName.of("com.example.Lambda"),
                "value",
                asList(value(2))));
    }

    @Test
    public void staticMethodOverloads() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "static-method-overloads",
                TypeName.of("com.example.StaticMethodOverloads"),
                "value",
                asList()));
    }

    private PrimitiveValue execTestProgram(
        String directoryName,
        TypeName type,
        String methodName,
        List<PrimitiveValue> arguments) throws IOException, InterruptedException {

        Path path = pathForResource(
            "/java/" + directoryName + "/" + type.getQualifiedName().replace(".", "/") + ".java");

        return execProgram(
            directoryName(path, type.getQualifiedName().split("\\.").length),
            type,
            methodName,
            arguments);
    }

    protected abstract PrimitiveValue execProgram(
        Path directory,
        TypeName type,
        String methodName,
        List<PrimitiveValue> arguments) throws IOException, InterruptedException;

    private Path directoryName(Path path, int length) {
        for (int i = 0; i < length; i++) {
            path = path.getParent();
        }
        return path;
    }

    private PrimitiveValue exec(String returnType, String source) {
        try {
            String javaClass =
                "package com.example;" +
                    "public class Example {" +
                    "    public static " + returnType + " main() {" + source + "}" +
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

    private PrimitiveValue evalBooleanExpression(String expressionSource) {
        return evalExpression("boolean", expressionSource);
    }

    private PrimitiveValue evalIntExpression(String expressionSource) {
        return evalExpression("int", expressionSource);
    }

    private PrimitiveValue evalObjectExpression(String expressionSource) {
        return evalExpression("Object", expressionSource);
    }

    private PrimitiveValue evalExpression(String type, String expressionSource) {
        return exec(type, "return " + expressionSource + ";");
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
