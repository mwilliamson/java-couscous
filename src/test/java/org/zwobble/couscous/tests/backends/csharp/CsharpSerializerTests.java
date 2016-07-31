package org.zwobble.couscous.tests.backends.csharp;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.sugar.SwitchNode;
import org.zwobble.couscous.backends.csharp.CsharpSerializer;
import org.zwobble.couscous.tests.TestIds;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.ArrayNode.array;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.CastNode.cast;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.EnumNode.declareEnum;
import static org.zwobble.couscous.ast.ExceptionHandlerNode.exceptionHandler;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.FormalTypeParameterNode.formalTypeParameter;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.InstanceReceiver.instanceReceiver;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.MethodSignature.signature;
import static org.zwobble.couscous.ast.Operations.*;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.ThrowNode.throwNode;
import static org.zwobble.couscous.ast.TryNode.tryStatement;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.ast.sugar.SwitchCaseNode.switchCase;
import static org.zwobble.couscous.types.TypeParameter.typeParameter;
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
    public void charLiteralsUseSingleQuotes() {
        assertEquals("'b'", serialize(literal('b')));
        assertEquals("'\\''", serialize(literal('\'')));
    }

    @Test
    public void stringLiteralsUseDoubleQuotes() {
        String output = serialize(literal("blah"));
        assertEquals("\"blah\"", output);
    }

    @Test
    public void typeLiteralUsesTypeOfOperator() {
        String output = serialize(literal(ScalarType.of("com.example.Example")));
        assertEquals("typeof(com.example.Example)", output);
    }

    @Test
    public void typeLiteralUsesAtSymbolToEscapeReservedIdentifiers() {
        String output = serialize(literal(ScalarType.of("internal")));
        assertEquals("typeof(@internal)", output);
    }

    @Test
    public void typeLiteralDoesntUseAtSymbolForReservedTypeIdentifiers() {
        String output = serialize(literal(ScalarType.of("string")));
        assertEquals("typeof(string)", output);
    }

    @Test
    public void variableReferenceWritesIdentifier() {
        String output = serialize(reference(var(TestIds.ANY_ID, "x", ScalarType.of("X"))));
        assertEquals("x", output);
    }

    @Test
    public void identifierIsEscapedWithAtSymbolIfItsAReservedWord() {
        String output = serialize(reference(var(TestIds.ANY_ID, "internal", ScalarType.of("X"))));
        assertEquals("@internal", output);
    }

    @Test
    public void thisReferenceUsesThisKeyword() {
        String output = serialize(thisReference(ScalarType.of("X")));
        assertEquals("this", output);
    }

    @Test
    public void emptyArraySpecifiesTypeOfArray() {
        String output = serialize(array(ScalarType.of("X"), list()));
        assertEquals("new X[] {}", output);
    }

    @Test
    public void arrayElementsAreSeparatedByComma() {
        String output = serialize(array(ScalarType.of("X"), list(literal(1), literal(2))));
        assertEquals("new X[] {1, 2}", output);
    }

    @Test
    public void assignmentSeparatesTargetAndValueWithEqualsSign() {
        String output = serialize(assign(
            reference(var(TestIds.ANY_ID, "x", Types.BOOLEAN)),
            literal(true)));
        assertEquals("x = true", output);
    }

    @Test
    public void ternaryConditionalWritesConditionAndBranches() {
        String output = serialize(ternaryConditional(literal(true), literal(1), literal(2)));
        assertEquals("true ? 1 : 2", output);
    }

    @Test
    public void staticMethodCallWithNoArgumentsWritesStaticReceiver() {
        String output = serialize(staticMethodCall(
            ScalarType.of("X"),
            "y",
            list(),
            ScalarType.of("Y")));
        assertEquals("X.y()", output);
    }

    @Test
    public void methodCallWithNoArgumentsWritesReceiver() {
        String output = serialize(methodCall(
            reference(var(TestIds.ANY_ID, "x", ScalarType.of("X"))),
            "y",
            list(),
            ScalarType.of("Y")));
        assertEquals("x.y()", output);
    }

    @Test
    public void methodCallWithArguments() {
        String output = serialize(methodCall(
            reference(var(TestIds.ANY_ID, "x", ScalarType.of("X"))),
            "y",
            list(literal(1), literal(2)),
            ScalarType.of("Y")));
        assertEquals("x.y(1, 2)", output);
    }

    @Test
    public void methodCallWithTypeParameters() {
        String output = serialize(methodCall(
            instanceReceiver(reference(var(TestIds.ANY_ID, "x", ScalarType.of("X")))),
            "y",
            list(ScalarType.of("Z")),
            list(),
            ScalarType.of("Y"),
            signature("y", list(typeParameter(Identifier.forType("X").method("y"), "T")), list(), ScalarType.of("Y"))));
        assertEquals("x.y<Z>()", output);
    }

    @Test
    public void constructorCallWithNoArguments() {
        String output = serialize(constructorCall(
            ScalarType.of("X"),
            list()));
        assertEquals("new X()", output);
    }

    @Test
    public void constructorCallWithArguments() {
        String output = serialize(constructorCall(
            ScalarType.of("X"),
            list(literal(1), literal(2))));
        assertEquals("new X(1, 2)", output);
    }

    @Test
    public void prefixExpression() {
        String output = serialize(not(
            reference(var(TestIds.ANY_ID, "x", Types.BOOLEAN))));
        assertEquals("!x", output);
    }

    @Test
    public void infixOperation() {
        String output = serialize(integerAdd(literal(1), literal(2)));
        assertEquals("1 + 2", output);
    }

    @Test
    public void instanceOfIsWrittenUsingIsOperator() {
        String output = serialize(instanceOf(
            reference(var(TestIds.ANY_ID, "x", Types.OBJECT)),
            ScalarType.of("X")));
        assertEquals("x is X", output);
    }

    @Test
    public void infixOperationIsBracketedWhenNecessary() {
        assertEquals("1 + 2 * 3", serialize(integerAdd(literal(1), integerMultiply(literal(2), literal(3)))));
        assertEquals("(1 + 2) * 3", serialize(integerMultiply(integerAdd(literal(1), literal(2)), literal(3))));
    }

    @Test
    public void fieldAccessSeparatesReceiverAndNameWithDot() {
        String output = serialize(fieldAccess(
            reference(var(TestIds.ANY_ID, "x", ScalarType.of("X"))),
            "y",
            ScalarType.of("Y")));
        assertEquals("x.y", output);
    }

    @Test
    public void staticFieldAccessSeparatesReceiverAndNameWithDot() {
        String output = serialize(fieldAccess(
            ScalarType.of("X"),
            "y",
            ScalarType.of("Y")));
        assertEquals("X.y", output);
    }

    @Test
    public void castIsWrittenWithParenthesisedType() {
        String output = serialize(cast(
            reference(var(TestIds.ANY_ID, "x", ScalarType.of("X"))),
            ScalarType.of("Y")));
        assertEquals("(Y) x", output);
    }

    @Test
    public void returnStatementUsesReturnStatement() {
        String output = serialize(returns(literal(true)));
        assertEquals("return true;\n", output);
    }

    @Test
    public void noValueIsSpecifiedWhenReturningUnit() {
        String output = serialize(returns(LiteralNode.UNIT));
        assertEquals("return;\n", output);
    }

    @Test
    public void throwStatementIncludesValue() {
        String output = serialize(throwNode(reference(var(TestIds.ANY_ID, "x", ScalarType.of("X")))));
        assertEquals("throw x;\n", output);
    }

    @Test
    public void expressionStatementWritesExpression() {
        String output = serialize(expressionStatement(literal(true)));
        assertEquals("true;\n", output);
    }

    @Test
    public void localVariableDeclarationCanDeclareVariable() {
        String output = serialize(localVariableDeclaration(
            TestIds.ANY_ID,
            "x",
            ScalarType.of("X"),
            literal("[value]")));
        assertEquals("X x = \"[value]\";\n", output);
    }

    @Test
    public void builtinTypesAreNotMangled() {
        String output = serialize(localVariableDeclaration(
            TestIds.ANY_ID,
            "x",
            ScalarType.of("string"),
            literal("[value]")));
        assertEquals("string x = \"[value]\";\n", output);
    }

    @Test
    public void ifElseStatementPrintsBothBranches() {
        String output = serialize(ifStatement(
            literal(true),
            list(returns(literal(1))),
            list(returns(literal(2)))));
        assertEquals("if (true) {\n    return 1;\n} else {\n    return 2;\n}\n", output);
    }

    @Test
    public void elseBranchIsNotGeneratedIfEmpty() {
        String output = serialize(ifStatement(
            literal(true),
            list(returns(literal(1))),
            list()));
        assertEquals("if (true) {\n    return 1;\n}\n", output);
    }

    @Test
    public void elseIfBranchesDontCauseNesting() {
        String output = serialize(ifStatement(
            literal(true),
            list(returns(literal(1))),
            list(ifStatement(
                literal(false),
                list(returns(literal(2))),
                list(returns(literal(3)))))));
        assertEquals("if (true) {\n    return 1;\n} else if (false) {\n    return 2;\n} else {\n    return 3;\n}\n", output);
    }

    @Test
    public void switchStatement() {
        Node node = new SwitchNode(
            literal(1),
            list(
                switchCase(literal(1), list(returns(literal(10)))),
                switchCase(list(returns(literal(0))))
            )
        );

        String output = serialize(node);

        assertEquals("switch (1) {\n    case 1:\n        return 10;\n    default:\n        return 0;\n}\n", output);
    }

    @Test
    public void whileLoopPrintsConditionAndBody() {
        String output = serialize(whileLoop(
            literal(true),
            list(returns(literal(1)))));
        assertEquals("while (true) {\n    return 1;\n}\n", output);
    }

    @Test
    public void statementBlockPrintsStatementsSurroundedByCurlyBraces() {
        String output = serialize(new StatementBlockNode(list(
            returns(literal(1))
        )));
        // TODO: remove leading space
        assertEquals(" {\n    return 1;\n}\n", output);
    }

    @Test
    public void tryStatementCanHaveBodyAndMultipleHandlers() {
        VariableDeclaration varX = var(TestIds.variable("x"), "x", ScalarType.of("X"));
        VariableDeclaration varY = var(TestIds.variable("y"), "y", ScalarType.of("Y"));
        String output = serialize(tryStatement(
            list(returns(literal(1))),
            list(
                exceptionHandler(varX, list(returns(reference(varX)))),
                exceptionHandler(varY, list(returns(reference(varY))))),
            list()));
        assertEquals("try {\n    return 1;\n} catch (X x) {\n    return x;\n} catch (Y y) {\n    return y;\n}\n", output);
    }

    @Test
    public void tryStatementCanHaveFinallyBody() {
        String output = serialize(tryStatement(
            list(),
            list(),
            list(returns(literal(1)))));
        assertEquals("try {\n} finally {\n    return 1;\n}\n", output);
    }

    @Test
    public void methodCanHaveVoidReturnType() {
        String output = serialize(MethodNode.staticMethod("nothing")
            .returns(ScalarType.of("void"))
            .build());
        assertEquals("public static void nothing() {\n}\n", output);
    }

    @Test
    public void instanceMethodHasNoStaticKeword() {
        String output = serialize(MethodNode.builder("nothing")
            .returns(ScalarType.of("void"))
            .build());
        assertEquals("public void nothing() {\n}\n", output);
    }

    @Test
    public void methodWithReturnType() {
        MethodNode methodNode = MethodNode.staticMethod("nothing")
            .returns(ScalarType.of("X"))
            .build();

        String output = serialize(methodNode);

        assertEquals("public static X nothing() {\n}\n", output);
    }

    @Test
    public void methodWithArguments() {
        MethodNode methodNode = MethodNode.staticMethod("nothing")
            .argument(formalArg(var(TestIds.variable("x"), "x", ScalarType.of("X"))))
            .argument(formalArg(var(TestIds.variable("y"), "y", ScalarType.of("Y"))))
            .returns(ScalarType.of("void"))
            .build();

        String output = serialize(methodNode);

        assertEquals("public static void nothing(X x, Y y) {\n}\n", output);
    }

    @Test
    public void methodHasSerializedBody() {
        MethodNode method = MethodNode.staticMethod("nothing")
            .returns(ScalarType.of("void"))
            .statement(returns(literal(true)))
            .build();
        String output = serialize(method);
        assertEquals("public static void nothing() {\n    return true;\n}\n", output);
    }

    @Test
    public void methodWithTypeParameters() {
        MethodNode methodNode = MethodNode.staticMethod("nothing")
            .typeParameter(formalTypeParameter(Identifier.forType("com.Example.example").method("nothing"), "T"))
            .typeParameter(formalTypeParameter(Identifier.forType("com.Example.example").method("nothing"), "U"))
            .returns(ScalarType.of("void"))
            .build();

        String output = serialize(methodNode);

        assertEquals("public static void nothing<T, U>() {\n}\n", output);
    }

    @Test
    public void classIsInNamespace() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .build();

        String output = serialize(classNode);

        assertEquals("namespace com.example {\n    internal class Example {\n    }\n}\n", output);
    }

    @Test
    public void namespacePartsInNamespaceDeclarationAreMangledInIfReserved() {
        ClassNode classNode = ClassNode.builder("com.example.internal.Example")
            .build();

        String output = serialize(classNode);

        assertEquals("namespace com.example.@internal {\n    internal class Example {\n    }\n}\n", output);
    }

    @Test
    public void namespacePartsInNamespaceReferenceAreMangledInIfReserved() {
        String output = serialize(localVariableDeclaration(
            TestIds.ANY_ID,
            "x",
            ScalarType.of("com.example.internal.Example"),
            literal("[value]")));
        assertEquals("com.example.@internal.Example x = \"[value]\";\n", output);
    }

    @Test
    public void classWithTypeParametersIsSplitIntoStaticAndInstanceClasses() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .addTypeParameter("T")
            .addTypeParameter("U")
            .field("x", ScalarType.of("X"))
            .staticField("y", ScalarType.of("Y"))
            .build();

        String output = serialize(classNode);

        assertEquals(
            "namespace com.example {" +
            "\n    internal static class Example {\n        internal static Y y;\n    }\n}" +
            "\nnamespace com.example {" +
            "\n    internal class Example<T, U> {\n        internal X x;\n    }\n}\n", output);
    }

    @Test
    public void classWithSuperTypes() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .addSuperType("com.example.Base")
            .build();

        String output = serialize(classNode);

        assertEquals("namespace com.example {\n    internal class Example : com.example.Base {\n    }\n}\n", output);
    }

    @Test
    public void staticConstructorHasSerializedBody() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .staticConstructor(list(expressionStatement(literal(true))))
            .build();

        String output = serialize(classNode);

        assertEquals(
            "namespace com.example {\n    internal class Example {\n        static Example() {\n            true;\n        }\n    }\n}\n", output);
    }

    @Test
    public void constructorHasSerializedBody() {
        ConstructorNode constructor = ConstructorNode.constructor(
            list(formalArg(var(TestIds.ANY_ID, "x", ScalarType.of("X")))),
            list(expressionStatement(literal(true))));
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .constructor(constructor)
            .build();

        String output = serialize(classNode);

        assertEquals(
            "namespace com.example {\n    internal class Example {\n        internal Example(X x) {\n            true;\n        }\n    }\n}\n", output);
    }

    @Test
    public void classWithFields() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .staticField("x", ScalarType.of("X"))
            .field("y", ScalarType.of("Y"))
            .build();

        String output = serialize(classNode);

        assertEquals("namespace com.example {\n    internal class Example {\n        internal static X x;\n        internal Y y;\n    }\n}\n", output);
    }

    @Test
    public void interfaceIsInNamespace() {
        Node node = new ClassNodeBuilder(ScalarType.of("com.example.Example")).buildInterface();

        String output = serialize(node);

        assertEquals("namespace com.example {\n    internal interface Example {\n    }\n}\n", output);
    }

    @Test
    public void interfaceWithTypeParameters() {
        Node node = new ClassNodeBuilder(ScalarType.of("com.example.Example"))
            .addTypeParameter("T")
            .addTypeParameter("U")
            .buildInterface();

        String output = serialize(node);

        assertEquals("namespace com.example {\n    internal interface Example<T, U> {\n    }\n}\n", output);
    }

    @Test
    public void interfaceWithSuperTypes() {
        Node node = new ClassNodeBuilder(ScalarType.of("com.example.Example"))
            .addSuperType("com.example.Base")
            .buildInterface();

        String output = serialize(node);

        assertEquals("namespace com.example {\n    internal interface Example : com.example.Base {\n    }\n}\n", output);
    }

    @Test
    public void interfaceWithMethod() {
        MethodNode method = MethodNode.builder("get").isAbstract().returns(ScalarType.of("X")).build();
        Node node = new ClassNodeBuilder(ScalarType.of("com.example.Example"))
            .method(method)
            .buildInterface();

        String output = serialize(node);

        assertEquals("namespace com.example {\n    internal interface Example {\n        X get();\n    }\n}\n", output);
    }

    @Test
    public void interfaceWithMethodWithTypeParameters() {
        MethodNode method = MethodNode.staticMethod("nothing")
            .typeParameter(formalTypeParameter(Identifier.forType("com.Example.example").method("nothing"), "T"))
            .typeParameter(formalTypeParameter(Identifier.forType("com.Example.example").method("nothing"), "U"))
            .returns(ScalarType.of("void"))
            .build();

        Node node = new ClassNodeBuilder(ScalarType.of("com.example.Example"))
            .method(method)
            .buildInterface();

        String output = serialize(node);

        assertEquals("namespace com.example {\n    internal interface Example {\n        void nothing<T, U>();\n    }\n}\n", output);
    }

    @Test
    public void enumWithSimpleValues() {
        Node node = declareEnum(ScalarType.of("com.example.Example"), list("ONE", "TWO"));

        String output = serialize(node);

        assertEquals("namespace com.example {\n    internal enum Example {\n        ONE, TWO\n    }\n}\n", output);
    }

    private String serialize(Node node) {
        return CsharpSerializer.serialize(node);
    }
}
