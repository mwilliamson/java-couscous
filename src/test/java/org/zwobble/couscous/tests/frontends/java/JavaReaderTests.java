package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.ObjectValues;
import org.zwobble.couscous.values.StringValue;

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
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.ExtraSets.set;

public class JavaReaderTests {
    @Test
    public void canDeclareAndReferenceLocalVariables() {
        List<StatementNode> statements = readStatements("int", "int x = 4; return x;");
        
        LocalVariableDeclarationNode declaration = (LocalVariableDeclarationNode) statements.get(0);
        assertEquals("x", declaration.getName());
        assertEquals(IntegerValue.REF, declaration.getType());
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
        assertEquals(typeCoercion(literal(4), ObjectValues.OBJECT), declaration.getInitialValue());
    }

    @Test
    public void returnTypeOfMethodIsRead() {
        ClassNode classNode = readClass(
            "public int identity(int value) {" +
            "    return value;" +
            "}");

        MethodNode method = classNode.getMethods().get(0);
        assertEquals(IntegerValue.REF, method.getReturnType());
    }
    
    @Test
    public void canDeclareAndReferenceArguments() {
        ClassNode classNode = readClass(
            "public int identity(int value) {" +
            "    return value;" +
            "}");
        
        MethodNode method = classNode.getMethods().get(0);
        FormalArgumentNode argument = method.getArguments().get(0);
        assertEquals("value", argument.getName());
        assertEquals(TypeName.of("int"), argument.getType());
        assertEquals(1, method.getArguments().size());
        
        ReturnNode returnNode = (ReturnNode)method.getBody().get().get(0);
        assertEquals(reference(argument), returnNode.getValue());
    }
    
    @Test
    public void canReadExpressionStatements() {
        assertEquals(
            expressionStatement(
                staticMethodCall(
                    TypeName.of("java.lang.Integer"),
                    "parseInt",
                    list(literal("42")),
                    IntegerValue.REF)),
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
                    thisReference(TypeName.of("com.example.Example")),
                    "name",
                    StringValue.REF),
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
                        thisReference(TypeName.of("com.example.Example")),
                        "year",
                        IntegerValue.REF),
                    literal(2013)),
                assignStatement(
                    fieldAccess(
                        thisReference(TypeName.of("com.example.Example")),
                        "name",
                        StringValue.REF),
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
                        thisReference(TypeName.of("com.example.Example")),
                        "year",
                        IntegerValue.REF),
                    literal(2013)),
                assignStatement(
                    fieldAccess(
                        thisReference(TypeName.of("com.example.Example")),
                        "name",
                        StringValue.REF),
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
                    TypeName.of("com.example.Example"),
                    "name",
                    StringValue.REF),
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
                    TypeName.of("com.example.Example"),
                    "name",
                    StringValue.REF),
                literal("Flaws"))),
            staticConstructor);
    }
    
    @Test
    public void canDeclareStaticMethodWithAnnotation() {
        ClassNode classNode = readClass(
            "@Deprecated public void doNothing() {}");
        
        MethodNode method = classNode.getMethods().get(0);
        assertEquals(
            list(annotation(TypeName.of("java.lang.Deprecated"))),
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
        assertEquals(TypeName.of("com.example.Example"), classNode.getName());
        assertEquals(list(), classNode.getMethods());
        assertEquals(set(ObjectValues.OBJECT), classNode.getSuperTypes());
    }

    @Test
    public void canReadInterfaceWithSuperTypes() {
        String source = "package com.example;" +
            "public interface Example extends java.util.function.IntSupplier {}";
        List<TypeNode> classes = readSource("com/example/Example.java", source);
        assertThat(classes, hasSize(1));
        InterfaceNode classNode = (InterfaceNode) classes.get(0);
        assertEquals(TypeName.of("com.example.Example"), classNode.getName());
        assertEquals(list(), classNode.getMethods());
        assertThat(classNode.getSuperTypes(), contains(ObjectValues.OBJECT, TypeName.of("java.util.function.IntSupplier")));
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
            .returns(IntegerValue.REF)
            .build();
        assertEquals(list(expectedMethod), classNode.getMethods());
    }
}
