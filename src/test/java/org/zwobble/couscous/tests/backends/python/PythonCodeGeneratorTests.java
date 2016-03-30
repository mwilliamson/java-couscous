package org.zwobble.couscous.tests.backends.python;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.couscous.backends.python.PythonCodeGenerator;
import org.zwobble.couscous.backends.python.ast.PythonListNode;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode;
import org.zwobble.couscous.types.Types;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.couscous.ast.ArrayNode.array;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.util.ExtraLists.list;

public class PythonCodeGeneratorTests {
    @Test
    public void arrayIsConvertedToPythonList() {
        assertThat(
            PythonCodeGenerator.generateExpression(array(Types.STRING, list(
                literal("one"),
                literal("two")))),
            isPythonList(list(isPythonLiteral("one"), isPythonLiteral("two"))));
    }

    private <T> Matcher<PythonExpressionNode> isPythonLiteral(String value) {
        return isA(PythonStringLiteralNode.class, hasFeature(
            "value",
            node -> node.getValue(),
            equalTo(value)));
    }

    private static <T, U> Matcher<T> isA(Class<U> type, Matcher<? super U> downcastMatcher) {
        return allOf(instanceOf(type), (Matcher<? super T>) downcastMatcher);
    }

    private static Matcher<PythonExpressionNode> isPythonList(
        List<Matcher<? super PythonExpressionNode>> expectedElements)
    {
        return isA(PythonListNode.class, hasFeature(
            "elements",
            list -> list.getElements(),
            contains(expectedElements)));
    }

    private static <T, U> Matcher<T> hasFeature(
        String name,
        Function<? super T, U> extract,
        Matcher<? super U> subMatcher) {
        return new FeatureMatcher<T, U>(subMatcher, name, name) {
            @Override
            protected U featureValueOf(T actual) {
                return extract.apply(actual);
            }
        };
    }
}
