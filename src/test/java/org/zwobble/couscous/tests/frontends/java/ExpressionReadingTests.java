package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifiers;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.TypeParameter;
import org.zwobble.couscous.types.Types;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.ArrayNode.array;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.CastNode.cast;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.FieldDeclarationNode.staticField;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.MethodSignature.signature;
import static org.zwobble.couscous.ast.Operations.*;
import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.TypeCoercionNode.typeCoercion;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.frontends.java.JavaReading.*;
import static org.zwobble.couscous.tests.frontends.java.NodeMatchers.*;
import static org.zwobble.couscous.types.ParameterizedType.parameterizedType;
import static org.zwobble.couscous.types.TypeParameter.typeParameter;
import static org.zwobble.couscous.util.ExtraLists.list;

public class ExpressionReadingTests {
    @Test
    public void canReadLiterals() {
        assertEquals(literal("hello"), readExpression("String", "\"hello\""));
        assertEquals(literal(true), readBooleanExpression("true"));
        assertEquals(literal(false), readBooleanExpression("false"));
        assertEquals(literal(42), readIntExpression("42"));
        assertEquals(literal('h'), readIntExpression("'h'"));
        assertEquals(literal(Types.STRING), readExpression("Class<String>", "String.class"));
    }

    @Test
    public void canReadEmptyArray() {
        assertEquals(array(Types.INT, list()), readExpression("int[]", "new int[]{}"));
    }

    @Test
    public void canReadArrayWithElements() {
        assertEquals(array(Types.INT, list(literal(42), literal(5))), readExpression("int[]", "new int[]{42, 5}"));
    }

    @Test
    public void canReadParenthesizedExpression() {
        assertEquals(literal(42), readIntExpression("(42)"));
    }

    @Test
    public void canReadThisReference() {
        assertEquals(
            thisReference(ScalarType.of("com.example.Example")),
            readExpressionInInstanceMethod("com.example.Example", "this"));
    }

    @Test
    public void canReadCasts() {
        assertEquals(
            cast(literal(42), Types.OBJECT),
            readExpression("Object", "(Object)42"));
    }
    @Test
    public void canReadFieldDeclarations() {
        ClassNode classNode = readClass(
            "private String name;");

        assertEquals(
            list(field("name", ScalarType.of("java.lang.String"))),
            classNode.getFields());
    }

    @Test
    public void canReadStaticFieldDeclarations() {
        ClassNode classNode = readClass(
            "private static String name;");

        assertEquals(
            list(staticField("name", ScalarType.of("java.lang.String"))),
            classNode.getFields());
    }

    @Test
    public void canReadFieldReferenceWithExplicitThis() {
        canReadFieldReference("this.name");
    }

    @Test
    public void canReadFieldReferenceWithImplicitThis() {
        canReadFieldReference("name");
    }

    private void canReadFieldReference(String expression) {
        ClassNode classNode = readClass(
            "private String name;" +
                "public String getName() {" +
                "    return " + expression + ";" +
                "}");

        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            fieldAccess(
                thisReference(ScalarType.of("com.example.Example")),
                "name",
                Types.STRING),
            returnNode.getValue());
    }

    @Test
    public void canReadFieldReferenceWithVariableReceiver() {
        ClassNode classNode = readClass(
            "private String name;" +
            "public String getName() {" +
            "    Example self = this;" +
            "    return self.name;" +
            "}");

        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get().get(1);
        assertThat(returnNode.getValue(), isFieldAccess(
            fieldHasReceiver(isVariableReference("self", ScalarType.of("com.example.Example"))),
            fieldHasName("name"),
            expressionHasType(Types.STRING)));
    }

    @Test
    public void canReadExplicitStaticFieldReferenceWithSimplyNamedClassAsReceiver() {
        canReadStaticFieldReference("Example.name");
    }

    @Test
    public void canReadExplicitStaticFieldReferenceWithFullyQualifiedClassAsReceiver() {
        canReadStaticFieldReference("com.example.Example.name");
    }

    @Test
    public void canReadExplicitStaticFieldReferenceWithThisAsReceiver() {
        canReadStaticFieldReference("this.name");
    }

    @Test
    public void canReadImplicitStaticFieldReference() {
        canReadStaticFieldReference("name");
    }

    private void canReadStaticFieldReference(String expression) {
        ClassNode classNode = readClass(
            "private static String name;" +
                "public String getName() {" +
                "    return " + expression + ";" +
                "}");

        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            fieldAccess(
                ScalarType.of("com.example.Example"),
                "name",
                Types.STRING),
            returnNode.getValue());
    }

    @Test
    public void canReadInstanceMethodCalls() {
        assertEquals(
            methodCall(literal("hello"), "startsWith", list(literal("h")), Types.BOOLEAN),
            readBooleanExpression("\"hello\".startsWith(\"h\")"));

        List<StatementNode> statements = readStatements("int", "Object x = 1; return x.hashCode();");
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        ReturnNode returnNode = (ReturnNode) statements.get(1);
        assertEquals(
            methodCall(reference(declaration), "hashCode", list(), Types.INT),
            returnNode.getValue());
    }

    @Test
    public void canReadImplicitInstanceMethodCalls() {
        ClassNode classNode = readClass(
            "public String loop() {" +
                "    return loop();" +
                "}");
        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get().get(0);

        assertEquals(
            methodCall(
                ThisReferenceNode.thisReference(ScalarType.of("com.example.Example")),
                "loop",
                list(),
                Types.STRING),
            returnNode.getValue());
    }

    @Test
    public void canReadStaticMethodCalls() {
        assertEquals(
            staticMethodCall(
                ScalarType.of("java.lang.Integer"),
                "parseInt",
                list(literal("42")),
                Types.INT),
            readIntExpression("Integer.parseInt(\"42\")"));
    }

    @Test
    public void canReadImplicitStaticMethodCalls() {
        ClassNode classNode = readClass(
            "public static String loop() {" +
                "    return loop();" +
                "}");
        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get().get(0);

        assertEquals(
            staticMethodCall(
                ScalarType.of("com.example.Example"),
                "loop",
                list(),
                Types.STRING),
            returnNode.getValue());
    }

    @Test
    public void canReadMethodCallsWithZeroExpressionsAsVarargs() {
        TypeParameter typeParameter = typeParameter(Identifiers.method(Identifiers.forType("java.util.Arrays"), "asList"), "T");
        assertEquals(
            methodCall(
                staticReceiver("java.util.Arrays"),
                "asList",
                list(Types.STRING),
                list(array(Types.STRING, list())),
                parameterizedType(ScalarType.of("java.util.List"), list(Types.STRING)),
                signature(
                    "asList",
                    list(typeParameter),
                    list(Types.array(typeParameter)),
                    parameterizedType(ScalarType.of("java.util.List"), list(typeParameter)))),
            readExpression("java.util.List<String>", "java.util.Arrays.asList()"));
    }

    @Test
    public void canReadMethodCallsWithMultipleExpressionsAsVarargs() {
        TypeParameter typeParameter = typeParameter(Identifiers.method(Identifiers.forType("java.util.Arrays"), "asList"), "T");
        assertEquals(
            methodCall(
                staticReceiver("java.util.Arrays"),
                "asList",
                list(Types.STRING),
                list(array(Types.STRING, list(literal("one"), literal("two")))),
                parameterizedType(ScalarType.of("java.util.List"), list(Types.STRING)),
                signature(
                    "asList",
                    list(typeParameter),
                    list(Types.array(typeParameter)),
                    parameterizedType(ScalarType.of("java.util.List"), list(typeParameter)))),
            readExpression("java.util.List<String>", "java.util.Arrays.asList(\"one\", \"two\")"));
    }

    @Test
    public void canReadMethodCallsWithArrayAsVarargs() {
        TypeParameter typeParameter = typeParameter(Identifiers.method(Identifiers.forType("java.util.Arrays"), "asList"), "T");
        assertEquals(
            methodCall(
                staticReceiver("java.util.Arrays"),
                "asList",
                list(Types.STRING),
                list(array(Types.STRING, list())),
                parameterizedType(ScalarType.of("java.util.List"), list(Types.STRING)),
                signature(
                    "asList",
                    list(typeParameter),
                    list(Types.array(typeParameter)),
                    parameterizedType(ScalarType.of("java.util.List"), list(typeParameter)))),
            readExpression("java.util.List<String>", "java.util.Arrays.asList(new String[]{})"));
    }

    @Test
    public void canReadGenericStaticMethodCallsWithExplicitTypeParameters() {
        TypeParameter typeParameter = typeParameter(Identifiers.method(Identifiers.forType("java.util.Collections"), "emptyList"), "T");
        assertEquals(
            methodCall(
                staticReceiver("java.util.Collections"),
                "emptyList",
                list(Types.STRING),
                list(),
                parameterizedType(ScalarType.of("java.util.List"), list(Types.STRING)),
                signature(
                    "emptyList",
                    list(typeParameter),
                    list(),
                    parameterizedType(ScalarType.of("java.util.List"), list(typeParameter)))),
            readExpression("java.util.List<String>", "java.util.Collections.<String>emptyList()"));
    }

    @Test
    public void canReadGenericStaticMethodCallsWithImplicitTypeParameters() {
        TypeParameter typeParameter = typeParameter(Identifiers.method(Identifiers.forType("java.util.Collections"), "emptyList"), "T");
        assertEquals(
            methodCall(
                staticReceiver("java.util.Collections"),
                "emptyList",
                list(Types.STRING),
                list(),
                parameterizedType(ScalarType.of("java.util.List"), list(Types.STRING)),
                signature(
                    "emptyList",
                    list(typeParameter),
                    list(),
                    parameterizedType(ScalarType.of("java.util.List"), list(typeParameter)))),
            readExpression("java.util.List<String>", "java.util.Collections.emptyList()"));
    }

    @Test
    public void canReadConstructorCalls() {
        assertEquals(
            constructorCall(ScalarType.of("java.lang.String"), list(literal("_"))),
            readExpression("String", "new String(\"_\")"));
    }

    @Test
    public void argumentIsBoxedIfNecessary() {
        MethodCallNode expression = (MethodCallNode) readExpression(
            "String",
            "java.util.Objects.toString(42)");
        assertEquals(
            typeCoercion(literal(42), Types.OBJECT),
            expression.getArguments().get(0));
    }

    @Test
    public void canUseOperatorsOnReferences() {
        assertEquals(
            same(
                constructorCall(ScalarType.of("java.lang.Object"), emptyList()),
                constructorCall(ScalarType.of("java.lang.Object"), emptyList())),
            readBooleanExpression("new Object() == new Object()"));

        assertEquals(
            not(same(
                constructorCall(ScalarType.of("java.lang.Object"), emptyList()),
                constructorCall(ScalarType.of("java.lang.Object"), emptyList()))),
            readBooleanExpression("new Object() != new Object()"));

        assertEquals(
            instanceOf(
                constructorCall(ScalarType.of("java.lang.Object"), emptyList()),
                ScalarType.of("java.lang.Object")),
            readBooleanExpression("new Object() instanceof Object"));
    }

    @Test
    public void canUseOperatorsOnBooleans() {
        assertEquals(
            booleanAnd(literal(true), literal(false)),
            readBooleanExpression("true && false"));

        assertEquals(
            booleanOr(literal(true), literal(false)),
            readBooleanExpression("true || false"));
    }

    @Test
    public void canUseOperatorsOnIntegers() {
        assertEquals(
            integerAdd(literal(1), literal(2)),
            readIntExpression("1 + 2"));
        assertEquals(
            integerSubtract(literal(1), literal(2)),
            readIntExpression("1 - 2"));
        assertEquals(
            integerMultiply(literal(1), literal(2)),
            readIntExpression("1 * 2"));
        assertEquals(
            integerMultiply(integerMultiply(literal(1), literal(2)), literal(3)),
            readIntExpression("1 * 2 * 3"));
        assertEquals(
            integerDivide(literal(1), literal(2)),
            readIntExpression("1 / 2"));
        assertEquals(
            integerMod(literal(1), literal(2)),
            readIntExpression("1 % 2"));

        assertEquals(
            equal(literal(1), literal(2)),
            readBooleanExpression("1 == 2"));
        assertEquals(
            notEqual(literal(1), literal(2)),
            readBooleanExpression("1 != 2"));
        assertEquals(
            greaterThan(literal(1), literal(2)),
            readBooleanExpression("1 > 2"));
        assertEquals(
            greaterThanOrEqual(literal(1), literal(2)),
            readBooleanExpression("1 >= 2"));
        assertEquals(
            lessThan(literal(1), literal(2)),
            readBooleanExpression("1 < 2"));
        assertEquals(
            lessThanOrEqual(literal(1), literal(2)),
            readBooleanExpression("1 <= 2"));
    }

    @Test
    public void equalityOperatorUnboxesIfExactlyOneOperandIsPrimitive() {
        assertEquals(
            equal(
                literal(1),
                unboxInt(constructorCall(ScalarType.of("java.lang.Integer"), list(literal(1))))),
            readBooleanExpression("1 == new Integer(1)"));

        assertEquals(
            notEqual(
                literal(1),
                unboxInt(constructorCall(ScalarType.of("java.lang.Integer"), list(literal(1))))),
            readBooleanExpression("1 != new Integer(1)"));
    }

    @Test
    public void equalityOperatorDoesNotUnboxIfBothOperandsAreBoxed() {
        assertEquals(
            same(
                typeCoercion(constructorCall(Types.BOXED_INT, list(literal(1))), Types.OBJECT),
                typeCoercion(constructorCall(Types.BOXED_INT, list(literal(2))), Types.OBJECT)),
            readBooleanExpression("new Integer(1) == new Integer(2)"));

        assertEquals(
            not(same(
                typeCoercion(constructorCall(Types.BOXED_INT, list(literal(1))), Types.OBJECT),
                typeCoercion(constructorCall(Types.BOXED_INT, list(literal(2))), Types.OBJECT))),
            readBooleanExpression("new Integer(1) != new Integer(2)"));
    }

    @Test
    public void integerOperatorUnboxesWhenBothOperandsAreBoxed() {
        assertEquals(
            integerAdd(
                unboxInt(constructorCall(ScalarType.of("java.lang.Integer"), list(literal(1)))),
                unboxInt(constructorCall(ScalarType.of("java.lang.Integer"), list(literal(2))))),
            readIntExpression("new Integer(1) + new Integer(2)"));
    }

    @Test
    public void canReadTernaryConditionals() {
        assertEquals(
            ternaryConditional(literal(true), literal(1), literal(2)),
            readIntExpression("true ? 1 : 2"));
    }

    @Test
    public void canReadAssignments() {
        ClassNode classNode = readClass(
            "private String name;" +
                "public String getName() {" +
                "    return name = \"blah\";" +
                "}");

        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            assign(
                fieldAccess(
                    thisReference(ScalarType.of("com.example.Example")),
                    "name",
                    Types.STRING),
                literal("blah")),
            returnNode.getValue());
    }

    @Test
    public void valuesOfAssignmentsAreTypeCoercedIfNecessary() {
        ClassNode classNode = readClass(
            "private Object value;" +
                "public Object getValue() {" +
                "    return value = 4;" +
                "}");

        ReturnNode returnNode = (ReturnNode) classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            assign(
                fieldAccess(
                    thisReference(ScalarType.of("com.example.Example")),
                    "value",
                    Types.OBJECT),
                typeCoercion(literal(4), Types.OBJECT)),
            returnNode.getValue());
    }

    @Test
    public void canReadPrefixBooleanNegation() {
        assertEquals(
            not(literal(true)),
            readBooleanExpression("!true"));
    }

    @Test
    public void canReadPrefixIncrement() {
        ClassNode classNode = readClass(
            "public void go(int value) {" +
                "    ++value;" +
                "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode argument = method.getArguments().get(0);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            assign(
                reference(argument),
                integerAdd(reference(argument), literal(1))),
            statement.getExpression());
    }

    @Test
    public void canReadCompoundAssignmentOperations() {
        canReadCompoundAssignmentOperation("+=", Operator.ADD);
        canReadCompoundAssignmentOperation("-=", Operator.SUBTRACT);
        canReadCompoundAssignmentOperation("*=", Operator.MULTIPLY);
        canReadCompoundAssignmentOperation("/=", Operator.DIVIDE);
        canReadCompoundAssignmentOperation("%=", Operator.MOD);
    }

    private void canReadCompoundAssignmentOperation(String symbol, Operator operator) {
        ClassNode classNode = readClass(
            "public void go(int value) {" +
                "    value " + symbol + " 2;" +
                "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode argument = method.getArguments().get(0);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            assign(
                reference(argument),
                integerOperation(operator, reference(argument), literal(2))),
            statement.getExpression());
    }

    @Test
    public void rightOperandOfCompoundAssignmentsIsUnboxedIfNecessary() {
        ClassNode classNode = readClass(
            "public void go(int left, Integer right) {" +
                "    left += right;" +
                "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode left = method.getArguments().get(0);
        FormalArgumentNode right = method.getArguments().get(1);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            assign(
                reference(left),
                integerAdd(reference(left), unboxInt(reference(right)))),
            statement.getExpression());
    }

    @Test
    public void leftOperandOfCompoundAssignmentsIsUnboxedIfNecessary() {
        ClassNode classNode = readClass(
            "public void go(Integer left, int right) {" +
                "    left += right;" +
                "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode left = method.getArguments().get(0);
        FormalArgumentNode right = method.getArguments().get(1);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            assign(
                reference(left),
                boxInt(integerAdd(unboxInt(reference(left)), reference(right)))),
            statement.getExpression());
    }

    @Test
    public void canReadPrefixDecrement() {
        ClassNode classNode = readClass(
            "public void go(int value) {" +
                "    --value;" +
                "}");

        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode argument = method.getArguments().get(0);
        ExpressionStatementNode statement = (ExpressionStatementNode)
            classNode.getMethods().get(0).getBody().get().get(0);
        assertEquals(
            assign(
                reference(argument),
                integerSubtract(reference(argument), literal(1))),
            statement.getExpression());
    }
}
