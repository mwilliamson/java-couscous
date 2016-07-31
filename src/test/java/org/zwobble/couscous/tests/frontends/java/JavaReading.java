package org.zwobble.couscous.tests.frontends.java;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.frontends.java.JavaReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraIterables.only;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaReading {
    static ExpressionNode readExpressionInInstanceMethod(String returnType, String expressionSource) {
        String javaClass =
            "public " + returnType + " main() {" +
                "    return " + expressionSource + ";" +
                "}";

        ClassNode classNode = readClass(javaClass);
        MethodNode method = only(classNode.getMethods());
        ReturnNode returnStatement = (ReturnNode) only(method.getBody().get());
        return returnStatement.getValue();
    }

    static ExpressionNode readBooleanExpression(String expressionSource) {
        return readExpression("boolean", expressionSource);
    }

    static ExpressionNode readIntExpression(String expressionSource) {
        return readExpression("int", expressionSource);
    }

    static ExpressionNode readExpression(String returnType, String expressionSource) {
        ReturnNode returnStatement = (ReturnNode) readStatement(returnType, "return " + expressionSource + ";");
        return returnStatement.getValue();
    }

    static StatementNode readStatement(String returnType, String statementSource) {
        return only(readStatements(returnType, statementSource));
    }

    static List<StatementNode> readStatements(String returnType, String statementsSource) {
        String javaClass = generateMethodSource(returnType, statementsSource);
        ClassNode classNode = readClass(javaClass);
        return only(classNode.getMethods()).getBody().get();
    }

    static ClassNode readClass(String classBody) {
        List<TypeNode> classes = readTypes(classBody);
        return (ClassNode) only(classes);
    }

    static List<TypeNode> readTypes(String classBody) {
        String javaClass = generateClassSource(classBody);
        return readSource("com/example/Example.java", javaClass);
    }

    static List<TypeNode> readSource(String path, String contents) {
        try {
            Path directoryPath = Files.createTempDirectory(null);
            try {
                Path sourcePath = directoryPath.resolve(path);
                Files.createDirectories(sourcePath.getParent());
                Files.write(sourcePath, list(contents));

                return JavaReader.readClassesFromFiles(list(directoryPath), list(directoryPath.resolve(path)));
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    static String generateMethodSource(String returnType, String statementsSource) {
        return "public static " + returnType + " main() throws Exception {" +
            statementsSource +
            "}";
    }

    private static String generateClassSource(String classBody) {
        return "package com.example;" +
            "public class Example {" +
            classBody +
            "}";
    }
}
