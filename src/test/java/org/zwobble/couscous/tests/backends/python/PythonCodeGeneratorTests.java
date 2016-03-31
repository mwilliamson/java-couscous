package org.zwobble.couscous.tests.backends.python;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.couscous.backends.python.PythonCodeGenerator;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonListNode;
import org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode;
import org.zwobble.couscous.types.Types;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.zwobble.couscous.ast.ArrayNode.array;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.tests.util.ExtraMatchers.hasFeature;
import static org.zwobble.couscous.tests.util.ExtraMatchers.isInstance;
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
        return isInstance(PythonStringLiteralNode.class, hasFeature(
            "value",
            node -> node.getValue(),
            equalTo(value)));
    }

    private static Matcher<PythonExpressionNode> isPythonList(
        List<Matcher<? super PythonExpressionNode>> expectedElements)
    {
        return isInstance(PythonListNode.class, hasFeature(
            "elements",
            list -> list.getElements(),
            contains(expectedElements)));
    }
}
