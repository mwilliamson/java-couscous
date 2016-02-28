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
                "java.util.function.Supplier<Integer> supplier = new java.util.function.Supplier<Integer>() {\n" +
                    "    public Integer get() {\n" +
                    "        return 42;\n" +
                    "    }\n" +
                    "};")),
            readTypes(generateMethodSource("void",
                "java.util.function.Supplier<Integer> supplier = () -> 42;")));
    }

    @Test
    public void lambdaWithNoArgumentsAndBlockBodyIsReadAsAnonymousClassCreation() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "java.util.function.Supplier<Integer> supplier = () -> 42;")),
            readTypes(generateMethodSource("void",
                "java.util.function.Supplier<Integer> supplier = () -> { return 42; };")));
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
    public void expressionMethodReferenceWithStaticReceiverIsReadAsLambda() {
        assertEquals(
            readTypes(generateMethodSource("void",
                "java.util.function.Function<String, Integer> function = arg0 -> Integer.parseInt(arg0);")),
            readTypes(generateMethodSource("void",
                "java.util.function.Function<String, Integer> function = Integer::parseInt;")));
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
