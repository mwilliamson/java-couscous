package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.tests.frontends.java.JavaReading.generateMethodSource;
import static org.zwobble.couscous.tests.frontends.java.JavaReading.readClasses;

public class InnerTypeReadingTests {
    @Test
    public void lambdaWithNoArgumentsAndExpressionBodyIsReadAsAnonymousClassCreation() {
        assertEquals(
            readClasses(generateMethodSource("void",
                "java.util.function.Supplier<Integer> supplier = new java.util.function.Supplier<Integer>() {\n" +
                    "    public Integer get() {\n" +
                    "        return 42;\n" +
                    "    }\n" +
                    "};")),
            readClasses(generateMethodSource("void",
                "java.util.function.Supplier<Integer> supplier = () -> 42;")));
    }

    @Test
    public void lambdaWithNoArgumentsAndBlockBodyIsReadAsAnonymousClassCreation() {
        assertEquals(
            readClasses(generateMethodSource("void",
                "java.util.function.Supplier<Integer> supplier = () -> 42;")),
            readClasses(generateMethodSource("void",
                "java.util.function.Supplier<Integer> supplier = () -> { return 42; };")));
    }

    @Test
    public void lambdaWithInferredArgumentsAndBlockBodyIsReadAsAnonymousClassCreation() {
        assertEquals(
            readClasses(generateMethodSource("void",
                "java.util.function.Function<Integer, Integer> function = x -> x;")),
            readClasses(generateMethodSource("void",
                "java.util.function.Function<Integer, Integer> function = (Integer x) -> x;")));
    }

    @Test
    public void expressionMethodReferenceWithStaticReceiverIsReadAsLambda() {
        assertEquals(
            readClasses(generateMethodSource("void",
                "java.util.function.Function<String, Integer> function = arg0 -> Integer.parseInt(arg0);")),
            readClasses(generateMethodSource("void",
                "java.util.function.Function<String, Integer> function = Integer::parseInt;")));
    }

    @Test
    public void expressionMethodReferenceWithInstanceReceiverIsReadAsLambda() {
        assertEquals(
            readClasses(generateMethodSource("void",
                "Integer x = 0; java.util.function.Supplier<String> function = () -> x.toString();")),
            readClasses(generateMethodSource("void",
                "Integer x = 0; java.util.function.Supplier<String> function = x::toString;")));
    }
}
