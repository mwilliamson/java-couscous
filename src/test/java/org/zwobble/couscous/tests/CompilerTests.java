package org.zwobble.couscous.tests;

import org.junit.Test;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.PrimitiveValue;

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
        assertEquals(value("hello"), evalExpression(Types.STRING, "\"hello\""));
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
        assertEquals(value(true), exec(Types.BOOLEAN, "Object x = new Object(); return x == x;"));
    }

    @Test
    public void integersCanBeAssignedToObjectVariable() {
        assertEquals(value(false), exec(Types.BOOLEAN, "Object x = 1; return x.equals(new Object());"));
        assertEquals(value(true), exec(Types.BOOLEAN, "Object x = 1; return x.equals(1);"));
    }

    @Test
    public void recursiveFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "recursive-factorial",
                ScalarType.topLevel("com.example.RecursiveFactorial"),
                "factorial",
                list(value(6)),
                Types.INT));
    }

    @Test
    public void whileFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "while-factorial",
                ScalarType.topLevel("com.example.WhileFactorial"),
                "factorial",
                list(value(6)),
                Types.INT));
    }

    @Test
    public void forFactorial() throws Exception {
        assertEquals(
            value(720),
            execTestProgram(
                "for-factorial",
                ScalarType.topLevel("com.example.ForFactorial"),
                "factorial",
                list(value(6)),
                Types.INT));
    }

    @Test
    public void forEach() throws Exception {
        assertEquals(
            value("abc"),
            execTestProgram(
                "for-each",
                ScalarType.topLevel("com.example.ForEach"),
                "value",
                list(),
                Types.STRING));
    }

    @Test
    public void staticNestedClasses() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "static-nested-class",
                ScalarType.topLevel("com.example.StaticNestedClass"),
                "value",
                list(),
                Types.INT));
    }

    @Test
    public void innerClasses() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "inner-class",
                ScalarType.topLevel("com.example.InnerClass"),
                "run",
                list(),
                Types.INT));
    }

    @Test
    public void innerClassesWithConstructorArguments() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "inner-class-with-constructor-args",
                ScalarType.topLevel("com.example.InnerClass"),
                "run",
                list(),
                Types.INT));
    }

    @Test
    public void canImplementUserDefinedInterface() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "implement-user-interface",
                ScalarType.topLevel("com.example.ConstantIntSupplier"),
                "value",
                list(),
                Types.INT));
    }

    @Test
    public void anonymousClassesWithoutCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "anonymous-class",
                ScalarType.topLevel("com.example.AnonymousClass"),
                "value",
                list(),
                Types.INT));
    }

    @Test
    public void anonymousClassesWithThisReference() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "anonymous-class-this-reference",
                ScalarType.topLevel("com.example.AnonymousClass"),
                "value",
                list(),
                Types.INT));
    }

    @Test
    public void anonymousClassesWithCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "anonymous-class-capture",
                ScalarType.topLevel("com.example.AnonymousClass"),
                "value",
                list(value(42)),
                Types.INT));
    }

    @Test
    public void anonymousClassesWithTypeCapture() throws Exception {
        assertEquals(
            value("Hello"),
            execTestProgram(
                "anonymous-class-type-capture",
                ScalarType.topLevel("com.example.AnonymousClass"),
                "value",
                list(),
                Types.STRING));
    }

    @Test
    public void lambdasWithoutCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda",
                ScalarType.topLevel("com.example.Lambda"),
                "value",
                list(),
                Types.INT));
    }

    @Test
    public void lambdasWithCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda-capture",
                ScalarType.topLevel("com.example.Lambda"),
                "value",
                list(value(2)),
                Types.INT));
    }

    @Test
    public void lambdasWithSameVariableCapturedMultipleTimes() throws Exception {
        assertEquals(
            value(84),
            execTestProgram(
                "lambda-multiple-capture",
                ScalarType.topLevel("com.example.Lambda"),
                "value",
                list(value(2)),
                Types.INT));
    }

    @Test
    public void lambdasWithThisCapture() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "lambda-this-capture",
                ScalarType.topLevel("com.example.Lambda"),
                "value",
                list(value(2)),
                Types.INT));
    }

    @Test
    public void genericMethods() throws Exception {
        assertEquals(
            value("two"),
            execTestProgram(
                "generic-method",
                ScalarType.topLevel("com.example.GenericMethod"),
                "value",
                list(),
                Types.STRING));
    }

    @Test
    public void genericInterfacesWithGenericTypeParameterAsReturnType() throws Exception {
        assertEquals(
            value("Hello"),
            execTestProgram(
                "generic-interface-return-type",
                ScalarType.topLevel("com.example.GenericInterface"),
                "value",
                list(),
                Types.STRING));
    }

    @Test
    public void genericInterfacesCanHaveMethodsWithoutGenericReturnType() throws Exception {
        assertEquals(
            value("Hello"),
            execTestProgram(
                "generic-interface-non-generic-methods",
                ScalarType.topLevel("com.example.GenericInterface"),
                "value",
                list(),
                Types.STRING));
    }

    @Test
    public void staticMethodOverloads() throws Exception {
        assertEquals(
            value(42),
            execTestProgram(
                "static-method-overloads",
                ScalarType.topLevel("com.example.StaticMethodOverloads"),
                "value",
                list(),
                Types.INT));
    }

    @Test
    public void switchStatements() throws Exception {
        switchStatementTest("simpleReturn", "one", 1);
        switchStatementTest("simpleReturn", "zero", 0);

        switchStatementTest("simpleFallthroughReturn", "one", 1);
        switchStatementTest("simpleFallthroughReturn", "two", 1);
        switchStatementTest("simpleFallthroughReturn", "zero", 0);

        switchStatementTest("noDefaultReturn", "one", 1);
        switchStatementTest("noDefaultReturn", "zero", 0);
    }

    private void switchStatementTest(String methodName, String input, int expected) throws Exception {
        assertEquals(
            value(expected),
            execTestProgram(
                "switch-statements",
                ScalarType.topLevel("com.example.SwitchStatements"),
                methodName,
                list(value(input)),
                Types.INT));
    }

    @Test
    public void stringAdd() throws Exception {
        assertEquals(value("42"), evalExpression(Types.STRING, "\"4\" + \"2\""));
    }

    @Test
    public void stringToLowerCase() throws Exception {
        assertEquals(value("patrick wolf"), evalExpression(Types.STRING, "\"Patrick Wolf\".toLowerCase()"));
    }

    @Test
    public void stringEquals() throws Exception {
        assertEquals(value(true), evalBooleanExpression("\"a\".equals(\"a\")"));
        assertEquals(value(false), evalBooleanExpression("\"a\".equals(\"b\")"));
        assertEquals(value(false), evalBooleanExpression("\"a\".equals(42)"));
    }

    private PrimitiveValue execTestProgram(
        String directoryName,
        ScalarType type,
        String methodName,
        List<PrimitiveValue> arguments,
        ScalarType returnType) throws IOException, InterruptedException {

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
        ScalarType type,
        String methodName,
        List<PrimitiveValue> arguments,
        ScalarType returnType) throws IOException, InterruptedException;

    private Path directoryName(Path path, int length) {
        for (int i = 0; i < length; i++) {
            path = path.getParent();
        }
        return path;
    }

    private PrimitiveValue exec(ScalarType returnType, String source) {
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
                    ScalarType.topLevel("com.example.Example"),
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
        return evalExpression(Types.BOOLEAN, expressionSource);
    }

    private PrimitiveValue evalIntExpression(String expressionSource) {
        return evalExpression(Types.INT, expressionSource);
    }

    private PrimitiveValue evalExpression(ScalarType type, String expressionSource) {
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
