package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.tests.frontends.java.JavaReading.generateMethodSource;
import static org.zwobble.couscous.tests.frontends.java.JavaReading.readTypes;

public class InnerTypeReadingTests {
    @Test
    public void lambdaWithNoArgumentsAndExpressionBodyIsReadAsAnonymousClassCreation() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "java.util.function.IntSupplier supplier = new java.util.function.IntSupplier() {\n" +
                    "    public int getAsInt() {\n" +
                    "        return 42;\n" +
                    "    }\n" +
                    "};")),
            readTypes(generateMethodSource("void",
                "java.util.function.IntSupplier supplier = () -> 42;")));
    }

    @Test
    public void lambdaWithNoArgumentsAndBlockBodyIsReadAsAnonymousClassCreation() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "java.util.function.IntSupplier supplier = () -> 42;")),
            readTypes(generateMethodSource("void",
                "java.util.function.IntSupplier supplier = () -> { return 42; };")));
    }

    @Test
    public void lambdaWithInferredArgumentsAndBlockBodyIsReadAsAnonymousClassCreation() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "java.util.function.Function<Integer, Integer> function = x -> x;")),
            readTypes(generateMethodSource("void",
                "java.util.function.Function<Integer, Integer> function = (Integer x) -> x;")));
    }

    @Test
    public void expressionStaticMethodReferenceWithStaticReceiverIsReadAsLambda() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "java.util.function.Function<String, Integer> function = arg0 -> Integer.parseInt(arg0);")),
            readTypes(generateMethodSource("void",
                "java.util.function.Function<String, Integer> function = Integer::parseInt;")));
    }

    @Test
    public void expressionInstanceMethodReferenceWithStaticReceiverIsReadAsLambda() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "java.util.function.Function<String, Integer> function = arg0 -> arg0.length();")),
            readTypes(generateMethodSource("void",
                "java.util.function.Function<String, Integer> function = String::length;")));
    }

    @Test
    public void expressionConstructorReferenceWithStaticReceiverIsReadAsLambda() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "java.util.function.Function<String, String> function = arg0 -> new String(arg0);")),
            readTypes(generateMethodSource("void",
                "java.util.function.Function<String, String> function = String::new;")));
    }

    @Test
    public void expressionMethodReferenceWithInstanceReceiverIsReadAsLambda() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "Integer x = 0; java.util.function.Supplier<String> function = () -> x.toString();")),
            readTypes(generateMethodSource("void",
                "Integer x = 0; java.util.function.Supplier<String> function = x::toString;")));
    }
}
