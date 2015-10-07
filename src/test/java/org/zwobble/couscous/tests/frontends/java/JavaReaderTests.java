package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.frontends.java.JavaReader;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.LiteralNode.literal;

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

    @SneakyThrows
    private ExpressionNode readExpression(String expressionSource) {
        val javaClass =
            "package com.example;" +
            "public class Example {" +
            "    public static Object main() {" +
            "        return " + expressionSource + ";" +
            "    }" +
            "}";
        
        val reader = new JavaReader();
        val classNode = reader.readClassFromString(javaClass);
        val returnStatement = (ReturnNode) classNode.getMethods().get(0).getBody().get(0);
        return returnStatement.getValue();
    }
}
