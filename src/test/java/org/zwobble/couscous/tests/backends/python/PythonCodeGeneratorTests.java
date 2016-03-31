package org.zwobble.couscous.tests.backends.python;

import org.junit.Test;
import org.zwobble.couscous.backends.python.PythonCodeGenerator;
import org.zwobble.couscous.types.Types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.couscous.ast.ArrayNode.array;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.tests.backends.python.PythonNodeMatchers.isPythonList;
import static org.zwobble.couscous.tests.backends.python.PythonNodeMatchers.isPythonLiteral;
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
}
