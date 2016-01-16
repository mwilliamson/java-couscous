package org.zwobble.couscous.tests.backends.csharp;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.backends.csharp.CsharpSerializer;
import org.zwobble.couscous.tests.TestIds;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.util.ExtraLists.list;

public class CsharpSerializerTests {
    @Test
    public void integerLiterals() {
        String output = serialize(literal(42));
        assertEquals("42", output);
    }

    @Test
    public void booleanLiterals() {
        String trueOutput = serialize(literal(true));
        assertEquals("true", trueOutput);

        String falseOutput = serialize(literal(false));
        assertEquals("false", falseOutput);
    }

    @Test
    public void stringLiteralsUseDoubleQuotes() {
        String output = serialize(literal("blah"));
        assertEquals("\"blah\"", output);
    }

    @Test
    public void typeLiteralUsesTypeOfOperator() {
        String output = serialize(literal(TypeName.of("com.example.Example")));
        assertEquals("typeof(Couscous.com.example.Example)", output);
    }

    @Test
    public void variableReferenceWritesIdentifier() {
        String output = serialize(reference(var(TestIds.ANY_ID, "x", TypeName.of("X"))));
        assertEquals("x", output);
    }

    @Test
    public void ternaryConditionalWritesConditionAndBranches() {
        String output = serialize(ternaryConditional(literal(true), literal(1), literal(2)));
        assertEquals("true ? 1 : 2", output);
    }

    @Test
    public void staticMethodCallWithNoArgumentsWritesStaticReceiver() {
        String output = serialize(staticMethodCall(
            TypeName.of("X"),
            "y",
            list(),
            TypeName.of("Y")));
        assertEquals("Couscous.X.y()", output);
    }

    @Test
    public void methodCallWithNoArgumentsWritesReceiver() {
        String output = serialize(methodCall(
            reference(var(TestIds.ANY_ID, "x", TypeName.of("X"))),
            "y",
            list(),
            TypeName.of("Y")));
        assertEquals("x.y()", output);
    }

    @Test
    public void methodCallWithArguments() {
        String output = serialize(methodCall(
            reference(var(TestIds.ANY_ID, "x", TypeName.of("X"))),
            "y",
            list(literal(1), literal(2)),
            TypeName.of("Y")));
        assertEquals("x.y(1, 2)", output);
    }

    @Test
    public void constructorCallWithNoArguments() {
        String output = serialize(constructorCall(
            TypeName.of("X"),
            list()));
        assertEquals("new Couscous.X()", output);
    }

    @Test
    public void constructorCallWithArguments() {
        String output = serialize(constructorCall(
            TypeName.of("X"),
            list(literal(1), literal(2))));
        assertEquals("new Couscous.X(1, 2)", output);
    }

    @Test
    public void returnStatementUsesReturnStatement() {
        String output = serialize(returns(literal(true)));
        assertEquals("return true;", output);
    }

    @Test
    public void methodHasDynamicReturnType() {
        String output = serialize(MethodNode.staticMethod("nothing").build());
        assertEquals("internal static dynamic nothing() {\n}\n", output);
    }

    @Test
    public void instanceMethodHasNoStaticKeword() {
        String output = serialize(MethodNode.builder("nothing").build());
        assertEquals("internal dynamic nothing() {\n}\n", output);
    }

    @Test
    public void methodWithArguments() {
        MethodNode methodNode = MethodNode.staticMethod("nothing")
            .argument(formalArg(var(TestIds.id("x"), "x", TypeName.of("X"))))
            .argument(formalArg(var(TestIds.id("y"), "y", TypeName.of("Y"))))
            .build();

        String output = serialize(methodNode);

        assertEquals("internal static dynamic nothing(dynamic x, dynamic y) {\n}\n", output);
    }

    @Test
    public void methodHasSerializedBody() {
        MethodNode method = MethodNode.staticMethod("nothing")
            .statement(returns(literal(true)))
            .build();
        String output = serialize(method);
        assertEquals("internal static dynamic nothing() {\n    return true;\n}\n", output);
    }

    @Test
    public void classIsInNamespace() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .build();

        String output = serialize(classNode);

        assertEquals("namespace Couscous.com.example {\n    internal class Example {\n    }\n}\n", output);
    }

    private String serialize(Node node) {
        return CsharpSerializer.serialize(node, "Couscous");
    }
}
