package org.zwobble.couscous.tests;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.zwobble.couscous.CouscousCompiler;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.PythonCompiler;
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
            execProgram(
                "recursive-factorial",
                TypeName.of("com.example.RecursiveFactorial"),
                "factorial",
                asList(value(6))));
    }
    
    private PrimitiveValue execProgram(
            String directory,
            TypeName type,
            String methodName,
            List<PrimitiveValue> arguments) throws IOException, InterruptedException {
        
        Path path = pathForResource(
            "/java/" + directory + "/" + type.getQualifiedName().replace(".", "/") + ".java");
        
        Path directoryPath = Files.createTempDirectory(null);
        try {
            CouscousCompiler compiler = new CouscousCompiler(
                new PythonCompiler(directoryPath, "couscous"));
            compiler.compileDirectory(directoryName(path, type.getQualifiedName().split(".").length));
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
                List<ClassNode> classNodes = JavaFrontend.readSourceDirectory(directoryPath);
                return new PythonMethodRunner().runMethod(classNodes, TypeName.of("com.example.Example"), "main", asList());
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