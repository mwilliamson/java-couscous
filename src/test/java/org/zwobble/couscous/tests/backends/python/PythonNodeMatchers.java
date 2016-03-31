package org.zwobble.couscous.tests.backends.python;

import org.hamcrest.Matcher;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonListNode;
import org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.zwobble.couscous.tests.util.ExtraMatchers.hasFeature;
import static org.zwobble.couscous.tests.util.ExtraMatchers.isInstance;

public class PythonNodeMatchers {
    public static Matcher<PythonExpressionNode> isPythonLiteral(String value) {
        return isInstance(PythonStringLiteralNode.class, hasFeature(
            "value",
            node -> node.getValue(),
            equalTo(value)));
    }

    public static Matcher<PythonExpressionNode> isPythonList(
        List<Matcher<? super PythonExpressionNode>> expectedElements)
    {
        return isInstance(PythonListNode.class, hasFeature(
            "elements",
            list -> list.getElements(),
            contains(expectedElements)));
    }
}
