package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.identifiers.Identifiers;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.zwobble.couscous.ast.AnnotationNode.annotation;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FormalTypeParameterNode.formalTypeParameter;
import static org.zwobble.couscous.ast.IfStatementNode.ifStatement;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.Operations.boxInt;
import static org.zwobble.couscous.ast.Operations.integerAdd;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.TypeCoercionNode.typeCoercion;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.tests.frontends.java.JavaReading.*;
import static org.zwobble.couscous.tests.frontends.java.NodeMatchers.isFormalArgument;
import static org.zwobble.couscous.tests.frontends.java.NodeMatchers.isFormalTypeParameter;
import static org.zwobble.couscous.types.TypeParameter.typeParameter;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.ExtraSets.set;

public class JavaReaderTests {
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

        Identifier classIdentifier = Identifiers.forType("com.example.Example");
        Identifier expectedMethodIdentifier = Identifiers.method(classIdentifier, "identity");
        assertThat(method.getTypeParameters(), contains(isFormalTypeParameter(expectedMethodIdentifier, "T")));
        assertThat(
            method.getArguments(),
            contains(isFormalArgument("value",  typeParameter(expectedMethodIdentifier, "T"))));
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
        List<StatementNode> statements = readStatements("int", "for (int x = 0; true; ++x) { return 1; }");
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals(
            list(
                localVariableDeclaration(declaration.getDeclaration(), literal(0)),
                whileLoop(
                    literal(true),
                    list(
                        returns(literal(1)),
                        expressionStatement(assign(declaration, integerAdd(reference(declaration), literal(1))))))),
            statements);
    }

    @Test
    public void switchStatementWithoutFallThroughIsReadAsIfElseStatement() {
        assertEquals(
            readStatements(
                "int",
                "String _couscous_tmp_0 = \"one\";" +
                "if (_couscous_tmp_0.equals(\"one\")) { return 0; }" +
                "else if (_couscous_tmp_0.equals(\"two\")) { return 0; }" +
                "else { return 1; }"),
            readStatements("int", "switch (\"one\") { case \"one\": case \"two\": return 0; default: return 1; }"));
    }

    @Test
    public void switchStatementWithoutDefaultIsReadAsIfStatementWithoutElseStatement() {
        assertEquals(
            readStatements(
                "int",
                "String _couscous_tmp_0 = \"one\";" +
                "if (_couscous_tmp_0.equals(\"one\")) { return 0; } return 1;"),
            readStatements("int", "switch (\"one\") { case \"one\": return 0; } return 1;"));
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
        assertEquals(list(formalTypeParameter(Identifiers.forType("com.example.Example"), "T")), classNode.getTypeParameters());
    }

    @Test
    public void canReadInterfaceWithGenericTypeParameters() {
        String source = "package com.example;" +
            "public interface Example<T> {}";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        InterfaceNode classNode = (InterfaceNode) classes.get(0);
        assertEquals(ScalarType.of("com.example.Example"), classNode.getName());
        assertEquals(list(formalTypeParameter(Identifiers.forType("com.example.Example"), "T")), classNode.getTypeParameters());
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
}
