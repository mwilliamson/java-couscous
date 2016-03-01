package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public abstract class CompilerTests {
    @Test
    public void canEvaluateLiterals() {
        assertEquals(value("hello"), evalExpression(StringValue.REF, "\"hello\""));
        assertEquals(value(42), evalIntExpression("42"));
    }

    @Test
    public void canUseOperatorsOnPrimitives() {
        assertEquals(value(3), evalIntExpression("1 + 2"));
        assertEquals(value(false), evalBooleanExpression("1 > 2"));
        assertEquals(value(true), evalBooleanExpression("1 == 1"));
        assertEquals(value(false), evalBooleanExpression("1 != 1"));
        assertEquals(value(true), evalBooleanExpression("!false"));
        assertEquals(value(true), evalBooleanExpression("true || false"));
        assertEquals(value(false), evalBooleanExpression("true && false"));
    }

    @Test
    public void equalityOnReferenceTypesChecksForIdentity() {
        assertEquals(value(false), evalBooleanExpression("new Object() == new Object()"));
        assertEquals(value(true), exec(BooleanValue.REF, "Object x = new Object(); return x == x;"));
    }

    @Test
    public void integersCanBeAssignedToObjectVariable() {
        assertEquals(value(false), exec(BooleanValue.REF, "Object x = 1; return x.equals(new Object());"));
        assertEquals(value(true), exec(BooleanValue.REF, "Object x = 1; return x.equals(1);"));
    }

    @Test
    public void recursiveFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "recursive-factorial",
                TypeName.of("com.example.RecursiveFactorial"),
                "factorial",
                list(value(6)),
                IntegerValue.REF));
    }

    @Test
    public void whileFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "while-factorial",
                TypeName.of("com.example.WhileFactorial"),
                "factorial",
                list(value(6)),
                IntegerValue.REF));
    }

    @Test
    public void forFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "for-factorial",
                TypeName.of("com.example.ForFactorial"),
                "factorial",
                list(value(6)),
                IntegerValue.REF));
    }

    @Test
    public void staticNestedClasses() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "static-nested-class",
                TypeName.of("com.example.StaticNestedClass"),
                "value",
                list(),
                IntegerValue.REF));
    }

    @Test
    public void innerClasses() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "inner-class",
                TypeName.of("com.example.InnerClass"),
                "run",
                list(),
                IntegerValue.REF));
    }

    @Test
    public void canImplementUserDefinedInterface() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "implement-user-interface",
                TypeName.of("com.example.ConstantIntSupplier"),
                "value",
                list(),
                IntegerValue.REF));
    }

    @Test
    public void anonymousClassesWithoutCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "anonymous-class",
                TypeName.of("com.example.AnonymousClass"),
                "value",
                list(),
                IntegerValue.REF));
    }

    @Test
    public void anonymousClassesWithCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "anonymous-class-capture",
                TypeName.of("com.example.AnonymousClass"),
                "value",
                list(value(42)),
                IntegerValue.REF));
    }

    @Test
    public void lambdasWithoutCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda",
                TypeName.of("com.example.Lambda"),
                "value",
                list(),
                IntegerValue.REF));
    }

    @Test
    public void lambdasWithCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda-capture",
                TypeName.of("com.example.Lambda"),
                "value",
                list(value(2)),
                IntegerValue.REF));
    }

    @Test
    public void lambdasWithThisCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda-this-capture",
                TypeName.of("com.example.Lambda"),
                "value",
                list(value(2)),
                IntegerValue.REF));
    }

    @Test
    public void staticMethodOverloads() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "static-method-overloads",
                TypeName.of("com.example.StaticMethodOverloads"),
                "value",
                list(),
                IntegerValue.REF));
    }

    @Test
    public void stringAdd() throws Exception {
        assertEquals(value("42"), evalExpression(StringValue.REF, "\"4\" + \"2\""));
    }

    @Test
    public void stringToLowerCase() throws Exception {
        assertEquals(value("patrick wolf"), evalExpression(StringValue.REF, "\"Patrick Wolf\".toLowerCase()"));
    }

    @Test
    public void stringEquals() throws Exception {
        assertEquals(value(true), evalBooleanExpression("\"a\".equals(\"a\")"));
        assertEquals(value(false), evalBooleanExpression("\"a\".equals(\"b\")"));
        assertEquals(value(false), evalBooleanExpression("\"a\".equals(42)"));
    }

    private PrimitiveValue execTestProgram(
        String directoryName,
        TypeName type,
        String methodName,
        List<PrimitiveValue> arguments,
        TypeName returnType) throws IOException, InterruptedException {

        Path path = pathForResource(
            "/java/" + directoryName + "/" + type.getQualifiedName().replace(".", "/") + ".java");

        return execProgram(
            directoryName(path, type.getQualifiedName().split("\\.").length),
            type,
            methodName,
            arguments,
            returnType);
    }

    protected abstract PrimitiveValue execProgram(
        Path directory,
        TypeName type,
        String methodName,
        List<PrimitiveValue> arguments,
        TypeName returnType) throws IOException, InterruptedException;

    private Path directoryName(Path path, int length) {
        for (int i = 0; i < length; i++) {
            path = path.getParent();
        }
        return path;
    }

    private PrimitiveValue exec(TypeName returnType, String source) {
        try {
            String javaClass =
                "package com.example;" +
                    "public class Example {" +
                    "    public static " + returnType.getQualifiedName() + " main() {" + source + "}" +
                    "}";
            Path directoryPath = Files.createTempDirectory(null);
            try {
                Files.createDirectories(directoryPath.resolve("com/example"));
                Files.write(directoryPath.resolve("com/example/Example.java"), list(javaClass));
                return execProgram(
                    directoryPath,
                    TypeName.of("com.example.Example"),
                    "main",
                    list(),
                    returnType);
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (final java.lang.Throwable $ex) {
            throw new RuntimeException($ex);
        }
    }

    private PrimitiveValue evalBooleanExpression(String expressionSource) {
        return evalExpression(BooleanValue.REF, expressionSource);
    }

    private PrimitiveValue evalIntExpression(String expressionSource) {
        return evalExpression(IntegerValue.REF, expressionSource);
    }

    private PrimitiveValue evalExpression(TypeName type, String expressionSource) {
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
