package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.frontends.java.JavaTypes;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.zwobble.couscous.ast.AnnotationNode.annotation;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FormalTypeParameterNode.formalTypeParameter;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.Operations.boxInt;
import static org.zwobble.couscous.ast.Operations.integerAdd;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.ThrowNode.throwNode;
import static org.zwobble.couscous.ast.TryNode.tryStatement;
import static org.zwobble.couscous.ast.TypeCoercionNode.typeCoercion;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.tests.frontends.java.JavaReading.*;
import static org.zwobble.couscous.tests.frontends.java.NodeMatchers.*;
import static org.zwobble.couscous.types.TypeParameter.typeParameter;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.ExtraSets.set;

public class JavaReaderTests {
    private static final Identifier MAIN_IDENTIFIER = Identifier.TOP.type("com.example.Example").method("main");

    @Test
    public void canReadReturnWithoutValue() {
        StatementNode statement = readStatement("void", "return;");
        assertEquals(statement, returns(LiteralNode.UNIT));
    }

    @Test
    public void canDeclareAndReferenceLocalVariables() {
        List<StatementNode> statements = readStatements("int", "int x = 4; return x;");
        
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals("x", declaration.getName());
        assertEquals(Types.INT, declaration.getType());
        assertEquals(literal(4), declaration.getInitialValue());
        ReturnNode returnNode = (ReturnNode) statements.get(1);
        assertEquals(
            reference(declaration),
            returnNode.getValue());
    }
    
    @Test
    public void initialValueOfLocalVariableDeclarationsAreTypeCoercedIfNecessary() {
        StatementNode statement = readStatement("void", "Object x = 4;");
        
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statement;
        assertEquals(typeCoercion(literal(4), Types.OBJECT), declaration.getInitialValue());
    }

    @Test
    public void returnTypeOfMethodIsRead() {
        ClassNode classNode = readClass(
            "public int identity(int value) {" +
            "    return value;" +
            "}");

        MethodNode method = classNode.getMethods().get(0);
        assertEquals(Types.INT, method.getReturnType());
    }
    
    @Test
    public void canDeclareAndReferenceArguments() {
        ClassNode classNode = readClass(
            "public int identity(int value) {" +
            "    return value;" +
            "}");
        
        MethodNode method = classNode.getMethods().get(0);
        assertThat(method.getArguments(), contains(isFormalArgument("value", ScalarType.of("int"))));
        
        ReturnNode returnNode = (ReturnNode)method.getBody().get().get(0);
        assertEquals(reference(method.getArguments().get(0)), returnNode.getValue());
    }

    @Test
    public void canReadTypeParametersForMethod() {
        ClassNode classNode = readClass(
            "public <T> T identity(T value) {" +
            "    return value;" +
            "}");

        MethodNode method = classNode.getMethods().get(0);

        Identifier classIdentifier = Identifier.forType("com.example.Example");
        Identifier expectedMethodIdentifier = classIdentifier.method("identity");
        assertThat(method.getTypeParameters(), contains(isFormalTypeParameter(expectedMethodIdentifier, "T")));
        assertThat(
            method.getArguments(),
            contains(isFormalArgument("value",  typeParameter(expectedMethodIdentifier, "T"))));
    }

    @Test
    public void canReadThrowStatements() {
        assertEquals(
            throwNode(
                constructorCall(ScalarType.of("java.lang.RuntimeException"), list())),
            readStatement("void", "throw new RuntimeException();"));
    }
    
    @Test
    public void canReadExpressionStatements() {
        assertEquals(
            expressionStatement(
                staticMethodCall(
                    ScalarType.of("java.lang.Integer"),
                    "parseInt",
                    list(literal("42")),
                    Types.INT)),
            readStatement("void", "Integer.parseInt(\"42\");"));
    }
    
    @Test
    public void canReadIfStatements() {
        assertEquals(
            ifStatement(
                literal(true),
                list(returns(literal(1))),
                list(returns(literal(2)))),
            readStatement("int", "if (true) { return 1; } else { return 2; }"));
    }

    @Test
    public void canReadIfStatementsWithoutElse() {
        assertEquals(
            list(
                ifStatement(
                    literal(true),
                    list(returns(literal(1))),
                    list()),
                returns(literal(2))),
            readStatements("int", "if (true) { return 1; } return 2;"));
    }

    @Test
    public void canReadWhileLoops() {
        assertEquals(
            whileLoop(
                literal(true),
                list(returns(literal(1)))),
            readStatement("int", "while (true) { return 1; }"));
    }

    @Test
    public void canReadForLoops() {
        StatementNode statement = readStatement("int", "for (int x = 0; true; ++x) { return 1; }");
        VariableDeclaration loopVariable = var(MAIN_IDENTIFIER.variable("x"), "x", Types.INT);
        assertEquals(
            new ForNode(
                list(localVariableDeclaration(loopVariable, literal(0))),
                literal(true),
                list(assign(reference(loopVariable), integerAdd(reference(loopVariable), literal(1)))),
                list(returns(literal(1)))
            ),
            statement
        );
    }

    @Test
    public void canReadForEachLoops() {
        List<StatementNode> statements = new JavaStatementSourceReader()
            .returns("String")
            .addVariable("iterable", "java.lang.Iterable<String>")
            .readStatement("for (String x : iterable) { return x; } return \"\";");

        VariableDeclaration expectedTarget = var(MAIN_IDENTIFIER.variable("x"), "x", Types.STRING);
        assertEquals(
            list(
                new ForEachNode(
                    expectedTarget,
                    reference(var(MAIN_IDENTIFIER.variable("iterable"), "iterable", JavaTypes.iterable(Types.STRING))),
                    list(returns(reference(expectedTarget)))
                ),
                returns(literal(""))
            ),
            statements
        );
    }

    @Test
    public void canReadSwitchStatements() {
        assertThat(
            readStatement("int", "switch (\"one\") { case \"one\": case \"two\": return 0; default: return 1; }"),
            isSwitch(
                hasSwitchValue(equalTo(literal("one"))),
                hasSwitchCases(
                    isCase(literal("one"), list()),
                    isCase(literal("two"), list(returns(literal(0)))),
                    isDefaultCase(list(returns(literal(1))))
                )
            )
        );
    }

    @Test
    public void canReadTryStatementsWithCatchClauses() {
        assertThat(
            readStatement("int", "try { return 1; } catch (Exception exception) { return 2; }"),
            isTryStatement(
                hasTryBody(list(returns(literal(1)))),
                hasExceptionHandlers(
                    isExceptionHandler(ScalarType.of("java.lang.Exception"), "exception", list(returns(literal(2)))))));
    }

    @Test
    public void canReadTryStatementsWithFinallyClauses() {
        assertThat(
            readStatement("int", "try { return 1; } finally { return 2; }"),
            isTryStatement(
                hasTryBody(list(returns(literal(1)))),
                hasFinally(returns(literal(2)))));
    }

    @Test
    public void canReadTryWithResourceWithoutOtherClauses() {
        ScalarType type = ScalarType.of("java.io.ByteArrayOutputStream");
        Identifier identifier = MAIN_IDENTIFIER.variable("closeable");
        LocalVariableDeclarationNode expectedResource = localVariableDeclaration(identifier, "closeable", type, constructorCall(type, list()));
        assertThat(
            readStatements("int", "try(java.io.ByteArrayOutputStream closeable = new java.io.ByteArrayOutputStream()) { return 1; }"),
            equalTo(list(
                expectedResource,
                tryStatement(
                    list(returns(literal(1))),
                    list(),
                    list(expressionStatement(methodCall(reference(expectedResource), "close", list(), Types.VOID)))
                )
            ))
        );
    }

    @Test
    public void canReadStatementBlocks() {
        List<StatementNode> statements = new JavaStatementSourceReader()
            .returns("int")
            .readStatement("{ return 0; }");

        assertEquals(
            list(new StatementBlockNode(list(returns(literal(0))))),
            statements
        );
    }

    @Test
    public void canDeclareConstructor() {
        ClassNode classNode = readClass(
            "private final String name;" +
            "public Example() {" +
            "    this.name = \"Flaws\";" +
            "}");
        
        ConstructorNode constructor = classNode.getConstructor();
        
        assertEquals(list(), constructor.getArguments());
        
        assertEquals(
            list(assignStatement(
                fieldAccess(
                    thisReference(ScalarType.of("com.example.Example")),
                    "name",
                    Types.STRING),
                literal("Flaws"))),
            constructor.getBody());
    }

    @Test
    public void instanceInitializerIsPrependedToConstructor() {
        ClassNode classNode = readClass(
            "private final String name;" +
            "private final int year;" +
            "public Example() {" +
            "    this.name = \"Flaws\";" +
            "}" +
            "{ year = 2013; }");

        ConstructorNode constructor = classNode.getConstructor();

        assertEquals(
            list(
                assignStatement(
                    fieldAccess(
                        thisReference(ScalarType.of("com.example.Example")),
                        "year",
                        Types.INT),
                    literal(2013)),
                assignStatement(
                    fieldAccess(
                        thisReference(ScalarType.of("com.example.Example")),
                        "name",
                        Types.STRING),
                    literal("Flaws"))),
            constructor.getBody());
    }

    @Test
    public void fieldInitializerIsPrependedToConstructor() {
        ClassNode classNode = readClass(
            "private final String name;" +
            "public Example() {" +
            "    this.name = \"Flaws\";" +
            "}" +
            "private final int year = 2013;");

        ConstructorNode constructor = classNode.getConstructor();

        assertEquals(
            list(
                assignStatement(
                    fieldAccess(
                        thisReference(ScalarType.of("com.example.Example")),
                        "year",
                        Types.INT),
                    literal(2013)),
                assignStatement(
                    fieldAccess(
                        thisReference(ScalarType.of("com.example.Example")),
                        "name",
                        Types.STRING),
                    literal("Flaws"))),
            constructor.getBody());
    }

    @Test
    public void canDeclareStaticInitializer() {
        ClassNode classNode = readClass(
            "private static final String name;" +
            "static {" +
            "    name = \"Flaws\";" +
            "}");

        List<StatementNode> staticConstructor = classNode.getStaticConstructor();

        assertEquals(
            list(assignStatement(
                fieldAccess(
                    ScalarType.of("com.example.Example"),
                    "name",
                    Types.STRING),
                literal("Flaws"))),
            staticConstructor);
    }

    @Test
    public void staticFieldInitialiserIsTreatedAsAssignmentInStaticConstructor() {
        ClassNode classNode = readClass(
            "private static final String name = \"Flaws\";");

        List<StatementNode> staticConstructor = classNode.getStaticConstructor();

        assertEquals(
            list(assignStatement(
                fieldAccess(
                    ScalarType.of("com.example.Example"),
                    "name",
                    Types.STRING),
                literal("Flaws"))),
            staticConstructor);
    }
    
    @Test
    public void canDeclareStaticMethodWithAnnotation() {
        ClassNode classNode = readClass(
            "@Deprecated public void doNothing() {}");
        
        MethodNode method = classNode.getMethods().get(0);
        assertEquals(
            list(annotation(ScalarType.of("java.lang.Deprecated"))),
            method.getAnnotations());
    }

    @Test
    public void returnValueHandlesBoxing() {
        ClassNode classNode = readClass(
            "public Integer one() { return 1; }");

        MethodNode method = classNode.getMethods().get(0);
        assertEquals(
            list(returns(boxInt(literal(1)))),
            method.getBody().get());
    }

    @Test
    public void canReadEmptyInterface() {
        String source = "package com.example;" +
            "public interface Example {}";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        InterfaceNode classNode = (InterfaceNode) classes.get(0);
        assertEquals(ScalarType.of("com.example.Example"), classNode.getName());
        assertEquals(list(), classNode.getMethods());
        assertEquals(set(), classNode.getSuperTypes());
        assertEquals(list(), classNode.getTypeParameters());
    }

    @Test
    public void canReadClassWithGenericTypeParameters() {
        String source = "package com.example;" +
            "public class Example<T> {}";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        ClassNode classNode = (ClassNode) classes.get(0);
        assertEquals(ScalarType.of("com.example.Example"), classNode.getName());
        assertEquals(list(formalTypeParameter(Identifier.forType("com.example.Example"), "T")), classNode.getTypeParameters());
    }

    @Test
    public void canReadInterfaceWithGenericTypeParameters() {
        String source = "package com.example;" +
            "public interface Example<T> {}";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        InterfaceNode classNode = (InterfaceNode) classes.get(0);
        assertEquals(ScalarType.of("com.example.Example"), classNode.getName());
        assertEquals(list(formalTypeParameter(Identifier.forType("com.example.Example"), "T")), classNode.getTypeParameters());
    }

    @Test
    public void canReadInterfaceWithSuperTypes() {
        String source = "package com.example;" +
            "public interface Example extends java.util.function.IntSupplier {}";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        InterfaceNode classNode = (InterfaceNode) classes.get(0);
        assertEquals(ScalarType.of("com.example.Example"), classNode.getName());
        assertEquals(list(), classNode.getMethods());
        assertThat(classNode.getSuperTypes(), contains(ScalarType.of("java.util.function.IntSupplier")));
    }

    @Test
    public void canReadInterfaceWithMethod() {
        String source = "package com.example;" +
            "public interface Example {" +
            "int get();" +
            "}";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        InterfaceNode classNode = (InterfaceNode) classes.get(0);

        MethodNode expectedMethod = MethodNode.builder("get")
            .isAbstract()
            .returns(Types.INT)
            .build();
        assertEquals(list(expectedMethod), classNode.getMethods());
    }

    @Test
    public void canReadEmptyEnum() {
        String source = "package com.example;" +
            "public enum Example {}";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        EnumNode node = (EnumNode) classes.get(0);
        assertEquals(ScalarType.of("com.example.Example"), node.getName());
    }

    @Test
    public void canReadEnumWithMembers() {
        String source = "package com.example;" +
            "public enum Example { ONE, TWO }";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        EnumNode node = (EnumNode) classes.get(0);
        assertEquals(ScalarType.of("com.example.Example"), node.getName());
        assertEquals(list("ONE", "TWO"), node.getValues());
    }

    @Test
    public void canReadUnicode() {
        ExpressionNode expression = readExpression("String", "\"π\"");
        assertEquals(literal("π"), expression);
    }
}
