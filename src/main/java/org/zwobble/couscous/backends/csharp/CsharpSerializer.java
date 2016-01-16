package org.zwobble.couscous.backends.csharp;

import com.google.common.base.Strings;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeVisitor;
import org.zwobble.couscous.backends.SourceCodeWriter;
import org.zwobble.couscous.values.PrimitiveValueVisitor;

public class CsharpSerializer implements NodeVisitor {
    public static String serialize(Node node, String namespace) {
        SourceCodeWriter writer = new SourceCodeWriter(
            (writer2, indentation) -> {
                writer2.writeSpace();
                writer2.writeSymbol("{");
            },
            (writer2, indentation) -> {
                writer2.writeSymbol(Strings.repeat(" ", indentation));
                writer2.writeSymbol("}");
                writer2.writeSymbol("\n");
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
                writer.writeIdentifier(namespace + "." + value.getQualifiedName());
                writer.writeSymbol(")");
                return null;
            }
        });
    }

    @Override
    public void visit(VariableReferenceNode variableReference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ThisReferenceNode reference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(AssignmentNode assignment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(TernaryConditionalNode ternaryConditional) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(MethodCallNode methodCall) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ConstructorCallNode call) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(FieldAccessNode fieldAccess) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(StaticReceiver receiver) {
        throw new UnsupportedOperationException();
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
    public void visit(FormalArgumentNode formalArgumentNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(AnnotationNode annotation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(MethodNode method) {
        writer.writeKeyword("internal");
        writer.writeSpace();
        writer.writeKeyword("static");
        writer.writeSpace();
        writer.writeKeyword("dynamic");
        writer.writeSpace();
        writer.writeIdentifier(method.getName());
        writer.writeSymbol("(");
        writer.writeSymbol(")");
        writer.startBlock();
        for (StatementNode statement : method.getBody()) {
            writer.writeStatement(() -> write(statement));
        }
        writer.endBlock();
    }

    @Override
    public void visit(ConstructorNode constructorNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(FieldDeclarationNode declaration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ClassNode classNode) {
        throw new UnsupportedOperationException();
    }
}
