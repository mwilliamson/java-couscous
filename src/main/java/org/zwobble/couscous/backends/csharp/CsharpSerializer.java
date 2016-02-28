package org.zwobble.couscous.backends.csharp;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeVisitor;
import org.zwobble.couscous.backends.SourceCodeWriter;
import org.zwobble.couscous.values.PrimitiveValueVisitor;
import org.zwobble.couscous.values.UnitValue;

import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.list;

public class CsharpSerializer implements NodeVisitor {
    public static String serialize(Node node) {
        SourceCodeWriter writer = new SourceCodeWriter(
            (writer2) -> {
                writer2.writeSpace();
                writer2.writeSymbol("{");
            },
            (writer2) -> {
                writer2.writeIndentation();
                writer2.writeSymbol("}");
            }
        );
        CsharpSerializer serializer = new CsharpSerializer(writer);

        serializer.write(node);
        return writer.asString();
    }

    private final SourceCodeWriter writer;

    private CsharpSerializer(SourceCodeWriter writer) {
        this.writer = writer;
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
        write(assignment.getTarget());
        writer.writeSpace();
        writer.writeSymbol("=");
        writer.writeSpace();
        write(assignment.getValue());
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

    @Override
    public void visit(OperationNode operation) {
        switch (operation.getOperatorType()) {
            case PREFIX:
                writer.writeSymbol(operation.getOperator().getSymbol());
                write(operation.getArguments().get(0));
                return;
            case INFIX:
                write(operation.getArguments().get(0));
                writer.writeSpace();
                writer.writeSymbol(operation.getOperator().getSymbol());
                writer.writeSpace();
                write(operation.getArguments().get(1));
                return;
            default:
                throw new RuntimeException("Unhandled operator type: " + operation.getOperatorType());
        }
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
        write(typeCoercion.getExpression());
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
        writer.writeStatement(() -> {
            writer.writeKeyword("return");
            if (!returnNode.getValue().getType().equals(UnitValue.REF)) {
                writer.writeSpace();
                write(returnNode.getValue());
            }
            writer.writeSymbol(";");
        });
    }

    @Override
    public void visit(ExpressionStatementNode expressionStatement) {
        writer.writeStatement(() -> {
            write(expressionStatement.getExpression());
            writer.writeSymbol(";");
        });
    }

    @Override
    public void visit(LocalVariableDeclarationNode localVariableDeclaration) {
        writer.writeStatement(() -> {
            writeTypeReference(localVariableDeclaration.getType());
            writer.writeSpace();
            writer.writeIdentifier(localVariableDeclaration.getName());
            writer.writeSpace();
            writer.writeSymbol("=");
            writer.writeSpace();
            write(localVariableDeclaration.getInitialValue());
            writer.writeSymbol(";");
        });
    }

    @Override
    public void visit(IfStatementNode ifStatement) {
        writer.writeStatement(() -> {
            writer.writeKeyword("if");
            writer.writeSpace();
            writer.writeSymbol("(");
            write(ifStatement.getCondition());
            writer.writeSymbol(")");

            writeBlock(ifStatement.getTrueBranch());

            writer.writeSpace();
            writer.writeKeyword("else");

            writeBlock(ifStatement.getFalseBranch());
        });
    }

    @Override
    public void visit(WhileNode whileLoop) {
        writer.writeStatement(() -> {
            writer.writeKeyword("while");
            writer.writeSpace();
            writer.writeSymbol("(");
            write(whileLoop.getCondition());
            writer.writeSymbol(")");

            writeBlock(whileLoop.getBody());
        });
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
            writeFormalArguments(method.getArguments());
            writeBlock(method.getBody());
        });
    }

    private void writeFormalArguments(List<FormalArgumentNode> arguments) {
        writer.writeSymbol("(");
        writer.writeWithSeparator(
            arguments,
            this::write,
            () -> writer.writeSymbol(", "));
        writer.writeSymbol(")");
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
            if (declaration.isStatic()) {
                writer.writeKeyword("static");
                writer.writeSpace();
            }
            writeTypeReference(declaration.getType());
            writer.writeSpace();
            writer.writeIdentifier(declaration.getName());
            writer.writeSymbol(";");
        });
    }

    @Override
    public void visit(ClassNode classNode) {
        writer.writeStatement(() -> {
            writer.writeKeyword("namespace");
            writer.writeSpace();
            writer.writeIdentifier(classNode.getName().getPackage().get());
            writer.startBlock();
            writer.writeStatement(() -> {
                writer.writeKeyword("internal");
                writer.writeSpace();
                writer.writeKeyword("class");
                writer.writeSpace();
                writer.writeIdentifier(classNode.getSimpleName());
                writer.startBlock();
                writeAll(classNode.getFields());
                writeStaticConstructor(classNode);
                writeConstructor(classNode, classNode.getConstructor());
                writeAll(classNode.getMethods());
                writer.endBlock();
            });

            writer.endBlock();
        });
    }

    @Override
    public void visit(InterfaceNode interfaceNode) {
        throw new UnsupportedOperationException();
    }

    private void writeStaticConstructor(ClassNode classNode) {
        if (!classNode.getStaticConstructor().isEmpty()) {
            writer.writeStatement(() -> {
                writer.writeKeyword("static");
                writer.writeSpace();
                writer.writeIdentifier(classNode.getSimpleName());
                writeFormalArguments(list());
                writeBlock(classNode.getStaticConstructor());
            });
        }
    }

    private void writeConstructor(ClassNode classNode, ConstructorNode constructor) {
        if (!constructor.equals(ConstructorNode.DEFAULT)) {
            writer.writeStatement(() -> {
                writer.writeKeyword("internal");
                writer.writeSpace();
                writer.writeIdentifier(classNode.getSimpleName());
                writeFormalArguments(constructor.getArguments());
                writeBlock(constructor.getBody());
            });
        }
    }

    private void writeBlock(List<StatementNode> body) {
        writer.startBlock();
        writeAll(body);
        writer.endBlock();
    }

    private void writeAll(List<? extends Node> nodes) {
        for (Node node : nodes) {
            write(node);
        }
    }

    private void writeTypeReference(TypeName value) {
        writer.writeIdentifier(typeReference(value));
    }

    private String typeReference(TypeName value) {
        return value.getQualifiedName();
    }
}
