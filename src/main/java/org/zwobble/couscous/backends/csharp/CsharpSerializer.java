package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeVisitor;
import org.zwobble.couscous.backends.SourceCodeWriter;
import org.zwobble.couscous.values.*;

import java.util.List;
import java.util.Map;

public class CsharpSerializer implements NodeVisitor {
    private final static Map<TypeName, String> PRIMITIVES = ImmutableMap.of(
        IntegerValue.REF, "int",
        StringValue.REF, "string",
        BooleanValue.REF, "bool",
        ObjectValues.CLASS, "System.Type",
        UnitValue.REF, "void");

    public static String serialize(Node node, String namespace) {
        SourceCodeWriter writer = new SourceCodeWriter(
            (writer2) -> {
                writer2.writeSpace();
                writer2.writeSymbol("{");
            },
            (writer2) -> {
                writer2.writeStatement(() -> writer2.writeSymbol("}"));
            }
        );
        CsharpSerializer serializer = new CsharpSerializer(writer, namespace);
        serializer.write(node);
        return writer.asString();
    }

    private final SourceCodeWriter writer;
    private final String namespace;

    private CsharpSerializer(SourceCodeWriter writer, String namespace) {
        this.writer = writer;
        this.namespace = namespace;
    }

    private void write(Node node) {
        node.accept(this);
    }

    @Override
    public void visit(LiteralNode literal) {
        literal.getValue().accept(new PrimitiveValueVisitor<Void>() {
            @Override
            public Void visitInteger(int value) {
                writer.writeInteger(value);
                return null;
            }

            @Override
            public Void visitString(String value) {
                writer.writeStringLiteral(value);
                return null;
            }

            @Override
            public Void visitBoolean(boolean value) {
                writer.writeKeyword(value ? "true" : "false");
                return null;
            }

            @Override
            public Void visitUnit() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Void visitType(TypeName value) {
                writer.writeKeyword("typeof");
                writer.writeSymbol("(");
                writeTypeReference(value);
                writer.writeSymbol(")");
                return null;
            }
        });
    }

    @Override
    public void visit(VariableReferenceNode variableReference) {
        writer.writeIdentifier(variableReference.getReferent().getName());
    }

    @Override
    public void visit(ThisReferenceNode reference) {
        writer.writeKeyword("this");
    }

    @Override
    public void visit(AssignmentNode assignment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(TernaryConditionalNode ternaryConditional) {
        write(ternaryConditional.getCondition());
        writer.writeSpace();
        writer.writeSymbol("?");
        writer.writeSpace();
        write(ternaryConditional.getIfTrue());
        writer.writeSpace();
        writer.writeSymbol(":");
        writer.writeSpace();
        write(ternaryConditional.getIfFalse());
    }

    @Override
    public void visit(MethodCallNode methodCall) {
        write(methodCall.getReceiver());
        writer.writeSymbol(".");
        writer.writeIdentifier(methodCall.getMethodName());
        writeArguments(methodCall.getArguments());
    }

    @Override
    public void visit(ConstructorCallNode call) {
        writer.writeKeyword("new");
        writer.writeSpace();
        writeTypeReference(call.getType());
        writeArguments(call.getArguments());
    }

    private void writeArguments(List<? extends ExpressionNode> arguments) {
        writer.writeSymbol("(");
        writer.writeWithSeparator(arguments, this::write, () -> writer.writeSymbol(", "));
        writer.writeSymbol(")");
    }

    @Override
    public void visit(FieldAccessNode fieldAccess) {
        write(fieldAccess.getLeft());
        writer.writeSymbol(".");
        writer.writeIdentifier(fieldAccess.getFieldName());
    }

    @Override
    public void visit(TypeCoercionNode typeCoercion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(CastNode cast) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(InstanceReceiver receiver) {
        write(receiver.getExpression());
    }

    @Override
    public void visit(StaticReceiver receiver) {
        writeTypeReference(receiver.getType());
    }

    @Override
    public void visit(ReturnNode returnNode) {
        writer.writeKeyword("return");
        writer.writeSpace();
        write(returnNode.getValue());
        writer.writeSymbol(";");
    }

    @Override
    public void visit(ExpressionStatementNode expressionStatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(LocalVariableDeclarationNode localVariableDeclaration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(IfStatementNode ifStatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(WhileNode whileLoop) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(FormalArgumentNode argument) {
        writeTypeReference(argument.getType());
        writer.writeSpace();
        writer.writeIdentifier(argument.getName());
    }

    @Override
    public void visit(AnnotationNode annotation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(MethodNode method) {
        writer.writeStatement(() -> {
            writer.writeKeyword("internal");
            writer.writeSpace();
            if (method.isStatic()) {
                writer.writeKeyword("static");
                writer.writeSpace();
            }
            writeTypeReference(method.getReturnType());
            writer.writeSpace();
            writer.writeIdentifier(method.getName());
            writer.writeSymbol("(");
            writer.writeWithSeparator(
                method.getArguments(),
                this::write,
                () -> writer.writeSymbol(", "));
            writer.writeSymbol(")");
            writer.startBlock();
            for (StatementNode statement : method.getBody()) {
                writer.writeStatement(() -> write(statement));
            }
            writer.endBlock();
        });
    }

    @Override
    public void visit(ConstructorNode constructorNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(FieldDeclarationNode declaration) {
        writer.writeStatement(() -> {
            writer.writeKeyword("internal");
            writer.writeSpace();
            writeTypeReference(declaration.getType());
            writer.writeSpace();
            writer.writeIdentifier(declaration.getName());
            writer.writeSymbol(";");
        });
    }

    @Override
    public void visit(ClassNode classNode) {
        String namespace = this.namespace +
            classNode.getName().getPackage()
                .map(packageName -> "." + packageName)
                .orElse("");
        writer.writeKeyword("namespace");
        writer.writeSpace();
        writer.writeIdentifier(namespace);
        writer.startBlock();
        writer.writeStatement(() -> {
            writer.writeKeyword("internal");
            writer.writeSpace();
            writer.writeKeyword("class");
            writer.writeSpace();
            writer.writeIdentifier(classNode.getSimpleName());
            writer.startBlock();
            writeAll(classNode.getFields());
            writeAll(classNode.getMethods());
            writer.endBlock();
        });

        writer.endBlock();
    }

    private void writeAll(List<? extends Node> nodes) {
        for (Node method : nodes) {
            write(method);
        }
    }

    private void writeTypeReference(TypeName value) {
        // TODO: process the nodes before serialization
        if (PRIMITIVES.containsKey(value)) {
            writer.writeIdentifier(PRIMITIVES.get(value));
        } else {
            writer.writeIdentifier(typeReference(value));
        }
    }

    private String typeReference(TypeName value) {
        return namespace + "." + value.getQualifiedName();
    }
}
