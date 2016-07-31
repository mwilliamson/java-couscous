package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableSet;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.sugar.SwitchCaseNode;
import org.zwobble.couscous.ast.sugar.SwitchNode;
import org.zwobble.couscous.ast.visitors.DynamicNodeMapper;
import org.zwobble.couscous.ast.visitors.DynamicNodeVisitor;
import org.zwobble.couscous.backends.SourceCodeWriter;
import org.zwobble.couscous.backends.csharp.primitives.PrimitiveInstanceMethodCall;
import org.zwobble.couscous.backends.csharp.primitives.PrimitiveStaticMethodCall;
import org.zwobble.couscous.types.*;
import org.zwobble.couscous.util.Action;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.util.ExtraIterables.lazyFilter;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.util.ExtraSets.set;

public class CsharpSerializer {
    private static final Set<String> RESERVED_TYPE_IDENTIFIERS = set(
        "bool",
        "byte",
        "char",
        "decimal",
        "double",
        "float",
        "int",
        "long",
        "object",
        "sbyte",
        "short",
        "string",
        "uint",
        "ulong",
        "ushort",
        "void");

    private static final Set<String> RESERVED_IDENTIFIERS;

    static {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        builder.add("abstract",
            "as",
            "base",
            "break",
            "case",
            "catch",
            "checked",
            "class",
            "const",
            "continue",
            "default",
            "delegate",
            "do",
            "else",
            "enum",
            "event",
            "explicit",
            "extern",
            "false",
            "finally",
            "fixed",
            "for",
            "foreach",
            "goto",
            "if",
            "implicit",
            "in",
            "interface",
            "internal",
            "is",
            "lock",
            "namespace",
            "new",
            "null",
            "operator",
            "out",
            "out (generic modifier)",
            "override",
            "params",
            "private",
            "protected",
            "public",
            "readonly",
            "ref",
            "return",
            "sealed",
            "sizeof",
            "stackalloc",
            "static",
            "struct",
            "switch",
            "this",
            "throw",
            "true",
            "try",
            "typeof",
            "unchecked",
            "unsafe",
            "using",
            "virtual",
            "volatile",
            "while");
        builder.addAll(RESERVED_TYPE_IDENTIFIERS);
        RESERVED_IDENTIFIERS = builder.build();
    }

    public static String serialize(Node node) {
        SourceCodeWriter writer = new SourceCodeWriter(
            (writer2) -> {
                writer2.writeSpace();
                writer2.writeSymbol("{");
            },
            (writer2) -> {
                writer2.writeIndentation();
                writer2.writeSymbol("}");
            },
            RESERVED_IDENTIFIERS,
            identifier -> "@" + identifier
        );
        CsharpSerializer serializer = new CsharpSerializer(writer);

        serializer.write(node);
        return writer.asString();
    }

    private final SourceCodeWriter writer;
    private final Consumer<Node> write;

    private CsharpSerializer(SourceCodeWriter writer) {
        this.writer = writer;
        this.write = DynamicNodeVisitor.instantiate(this, "visit");
    }

    private void write(Node node) {
        write.accept(node);
    }

    public void visit(PrimitiveStaticMethodCall expression) {
        write(expression.generate());
    }

    public void visit(PrimitiveInstanceMethodCall expression) {
        write(expression.generate());
    }

    public void visit(LiteralNode literal) {
        literal.getValue().accept(new PrimitiveValue.Visitor<Void>() {
            @Override
            public Void visitInteger(int value) {
                writer.writeInteger(value);
                return null;
            }

            @Override
            public Void visitChar(char value) {
                writer.writeChar(value);
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
            public Void visitType(ScalarType value) {
                writer.writeKeyword("typeof");
                writer.writeSymbol("(");
                writeTypeReference(value);
                writer.writeSymbol(")");
                return null;
            }
        });
    }

    public void visit(VariableReferenceNode variableReference) {
        writer.writeIdentifier(variableReference.getReferent().getName());
    }

    public void visit(ThisReferenceNode reference) {
        writer.writeKeyword("this");
    }

    public void visit(ArrayNode array) {
        writer.writeKeyword("new");
        writer.writeSpace();
        writeTypeReference(array.getElementType());
        writer.writeSymbol("[");
        writer.writeSymbol("]");
        writer.writeSpace();
        writer.writeSymbol("{");
        writer.writeCommaSeparated(array.getElements(), this::write);
        writer.writeSymbol("}");
    }

    public void visit(AssignmentNode assignment) {
        write(assignment.getTarget());
        writer.writeSpace();
        writer.writeSymbol("=");
        writer.writeSpace();
        writeParenthesized(assignment.getValue(), assignment);
    }

    public void visit(TernaryConditionalNode ternaryConditional) {
        writeParenthesized(ternaryConditional.getCondition(), ternaryConditional);
        writer.writeSpace();
        writer.writeSymbol("?");
        writer.writeSpace();
        writeParenthesized(ternaryConditional.getIfTrue(), ternaryConditional);
        writer.writeSpace();
        writer.writeSymbol(":");
        writer.writeSpace();
        writeParenthesized(ternaryConditional.getIfFalse(), ternaryConditional);
    }

    public void visit(MethodCallNode methodCall) {
        writeParenthesized(methodCall.getReceiver(), methodCall);
        writer.writeSymbol(".");
        writer.writeIdentifier(methodCall.getMethodName());
        writeTypeParameters(methodCall.getTypeParameters());
        writeArguments(methodCall.getArguments());
    }

    private void writeTypeParameters(List<Type> typeParameters) {
        if (!typeParameters.isEmpty()) {
            writer.writeSymbol("<");
            writer.writeCommaSeparated(typeParameters, this::writeTypeReference);
            writer.writeSymbol(">");
        }
    }

    public void visit(ConstructorCallNode call) {
        writer.writeKeyword("new");
        writer.writeSpace();
        writeTypeReference(call.getType());
        writeArguments(call.getArguments());
    }

    public void visit(OperationNode operation) {
        switch (operation.getOperatorType()) {
            case PREFIX:
                writer.writeSymbol(operation.getOperator().getSymbol());
                writeParenthesized(operation.getArguments().get(0), operation);
                return;
            case INFIX:
                writeParenthesized(operation.getArguments().get(0), operation);
                writer.writeSpace();
                writer.writeSymbol(operation.getOperator().getSymbol());
                writer.writeSpace();
                writeParenthesized(operation.getArguments().get(1), operation);
                return;
            default:
                throw new RuntimeException("Unhandled operator type: " + operation.getOperatorType());
        }
    }

    public void visit(InstanceOfNode instanceOf) {
        writeParenthesized(instanceOf.getLeft(), instanceOf);
        writer.writeSpace();
        writer.writeKeyword("is");
        writer.writeSpace();
        writeTypeReference(instanceOf.getRight());
    }

    private void writeArguments(List<? extends ExpressionNode> arguments) {
        writer.writeSymbol("(");
        writer.writeCommaSeparated(arguments, this::write);
        writer.writeSymbol(")");
    }

    public void visit(FieldAccessNode fieldAccess) {
        writeParenthesized(fieldAccess.getLeft(), fieldAccess);
        writer.writeSymbol(".");
        writer.writeIdentifier(fieldAccess.getFieldName());
    }

    public void visit(TypeCoercionNode typeCoercion) {
        write(typeCoercion.getExpression());
    }

    public void visit(CastNode cast) {
        writer.writeSymbol("(");
        writeTypeReference(cast.getType());
        writer.writeSymbol(")");
        writer.writeSpace();
        writeParenthesized(cast.getExpression(), cast);
    }

    public void visit(InstanceReceiver receiver) {
        write(receiver.getExpression());
    }

    public void visit(StaticReceiver receiver) {
        writeTypeReference(receiver.getType());
    }

    public void visit(ReturnNode returnNode) {
        writer.writeStatement(() -> {
            writer.writeKeyword("return");
            if (!returnNode.getValue().getType().equals(Types.VOID)) {
                writer.writeSpace();
                write(returnNode.getValue());
            } else if (!returnNode.getValue().equals(LiteralNode.UNIT)) {
                throw new UnsupportedOperationException();
            }
            writer.writeSymbol(";");
        });
    }

    public void visit(ThrowNode throwNode) {
        writer.writeStatement(() -> {
            writer.writeKeyword("throw");
            writer.writeSpace();
            write(throwNode.getValue());
            writer.writeSymbol(";");
        });
    }

    public void visit(ExpressionStatementNode expressionStatement) {
        writer.writeStatement(() -> {
            write(expressionStatement.getExpression());
            writer.writeSymbol(";");
        });
    }

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

    public void visit(IfStatementNode ifStatement) {
        writer.writeStatement(() -> {
            writeIfContent(ifStatement);
        });
    }

    private void writeIfContent(IfStatementNode ifStatement) {
        writer.writeKeyword("if");
        writer.writeSpace();
        writer.writeSymbol("(");
        write(ifStatement.getCondition());
        writer.writeSymbol(")");

        writeBlock(ifStatement.getTrueBranch());

        if (!ifStatement.getFalseBranch().isEmpty()) {
            writer.writeSpace();
            writer.writeKeyword("else");

            if (ifStatement.getFalseBranch().size() == 1 && ifStatement.getFalseBranch().get(0) instanceof IfStatementNode) {
                writer.writeSpace();
                writeIfContent((IfStatementNode) ifStatement.getFalseBranch().get(0));
            } else {
                writeBlock(ifStatement.getFalseBranch());
            }
        }
    }

    public void visit(SwitchNode switchNode) {
        writer.writeStatement(() -> {
            writer.writeKeyword("switch");
            writer.writeSpace();
            writer.writeSymbol("(");
            write(switchNode.getValue());
            writer.writeSymbol(")");
            writer.startBlock();
            switchNode.getCases().forEach(this::visit);
            writer.endBlock();
        });
    }

    private void visit(SwitchCaseNode switchCase) {
        writer.writeStatement(() -> {
            if (switchCase.getValue().isPresent()) {
                writer.writeKeyword("case");
                writer.writeSpace();
                write(switchCase.getValue().get());
            } else {
                writer.writeKeyword("default");
            }
            writer.writeSymbol(":");
        });
        writer.indent();
        writeAll(switchCase.getStatements());
        writer.dedent();

    }

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

    public void visit(StatementBlockNode block) {
        writer.writeStatement(() -> {
            writeBlock(block.getStatements());
        });
    }

    public void visit(TryNode tryStatement) {
        writer.writeStatement(() -> {
            writer.writeKeyword("try");
            writeBlock(tryStatement.getBody());
            for (ExceptionHandlerNode handler : tryStatement.getExceptionHandlers()) {
                writer.writeSpace();
                writer.writeKeyword("catch");
                writer.writeSpace();
                writer.writeSymbol("(");
                writeTypeReference(handler.getExceptionType());
                writer.writeSpace();
                writer.writeIdentifier(handler.getExceptionName());
                writer.writeSymbol(")");
                writeBlock(handler.getBody());
            }
            if (!tryStatement.getFinallyBody().isEmpty()) {
                writer.writeSpace();
                writer.writeKeyword("finally");
                writeBlock(tryStatement.getFinallyBody());
            }
        });
    }

    public void visit(FormalTypeParameterNode parameter) {
        writer.writeIdentifier(parameter.getName());
    }

    public void visit(FormalArgumentNode argument) {
        writeTypeReference(argument.getType());
        writer.writeSpace();
        writer.writeIdentifier(argument.getName());
    }

    public void visit(AnnotationNode annotation) {
        throw new UnsupportedOperationException();
    }

    public void visit(MethodNode method) {
        writer.writeStatement(() -> {
            writer.writeKeyword("public");
            writer.writeSpace();
            if (method.isStatic()) {
                writer.writeKeyword("static");
                writer.writeSpace();
            }
            writeSignature(method);
            writeBlock(method.getBody().get());
        });
    }

    private void writeSignature(MethodNode method) {
        writeTypeReference(method.getReturnType());
        writer.writeSpace();
        writer.writeIdentifier(method.getName());
        writeFormalTypeParameters(method.getTypeParameters());
        writeFormalArguments(method.getArguments());
    }

    private void writeFormalTypeParameters(List<FormalTypeParameterNode> typeParameters) {
        if (!typeParameters.isEmpty()) {
            writer.writeSymbol("<");
            writer.writeCommaSeparated(typeParameters, this::write);
            writer.writeSymbol(">");
        }
    }

    private void writeFormalArguments(List<FormalArgumentNode> arguments) {
        writer.writeSymbol("(");
        writer.writeCommaSeparated(arguments, this::write);
        writer.writeSymbol(")");
    }

    public void visit(ConstructorNode constructorNode) {
        throw new UnsupportedOperationException();
    }

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

    public void visit(ClassNode classNode) {
        if (classNode.getTypeParameters().isEmpty()) {
            writeInstanceType(classNode, "class", () -> {
                writeAll(classNode.getFields());
                writeStaticConstructor(classNode);
                writeConstructor(classNode, classNode.getConstructor());
                writeAll(classNode.getMethods());
            });
        } else {
            writeInNamespace(classNode, () -> {
                writer.writeStatement(() -> {
                    writer.writeKeyword("internal");
                    writer.writeSpace();
                    writer.writeKeyword("static");
                    writer.writeSpace();
                    writer.writeKeyword("class");
                    writer.writeSpace();
                    writer.writeIdentifier(classNode.getName().getSimpleName());
                    writer.startBlock();
                    writeAll(lazyFilter(classNode.getFields(), field -> field.isStatic()));
                    writeStaticConstructor(classNode);
                    writeAll(lazyFilter(classNode.getMethods(), method -> method.isStatic()));
                    writer.endBlock();
                });
            });
            writeInstanceType(classNode, "class", () -> {
                writeAll(lazyFilter(classNode.getFields(), field -> !field.isStatic()));
                writeConstructor(classNode, classNode.getConstructor());
                writeAll(lazyFilter(classNode.getMethods(), method -> !method.isStatic()));
            });
        }
    }

    public void visit(InterfaceNode interfaceNode) {
        writeInstanceType(interfaceNode, "interface", () -> {
            interfaceNode.getMethods().forEach(method -> {
                writer.writeStatement(() -> {
                    writeSignature(method);
                    writer.writeSymbol(";");
                });
            });
        });
    }

    public void visit(EnumNode enumNode) {
        writeInstanceType(enumNode, "enum", () -> {
            writer.writeStatement(() -> {
                writer.writeCommaSeparated(
                    enumNode.getValues(),
                    value -> writer.writeIdentifier(value));
            });
        });
    }

    private void writeInstanceType(TypeNode node, String keyword, Action writeBody) {
        writeInNamespace(node, () -> {
            writer.writeStatement(() -> {
                writer.writeKeyword("internal");
                writer.writeSpace();
                writer.writeKeyword(keyword);
                writer.writeSpace();
                writer.writeIdentifier(node.getName().getSimpleName());
                writeTypeParameters(node);
                writeSuperTypes(node);
                writer.startBlock();
                writeBody.run();
                writer.endBlock();
            });
        });
    }

    private void writeInNamespace(TypeNode typeNode, Action writeBody) {
        writeInNamespace(typeNode.getName().getPackage().get(), writeBody);
    }

    private void writeInNamespace(String namespace, Action writeBody) {
        writer.writeStatement(() -> {
            writer.writeKeyword("namespace");
            writer.writeSpace();
            writeQualifiedName(namespace);
            writer.startBlock();
            writeBody.run();
            writer.endBlock();
        });
    }

    private void writeTypeParameters(TypeNode node) {
        if (!node.getTypeParameters().isEmpty()) {
            writer.writeSymbol("<");
            writer.writeCommaSeparated(
                node.getTypeParameters(),
                parameter -> writer.writeIdentifier(parameter.getName()));
            writer.writeSymbol(">");
        }
    }

    private void writeSuperTypes(TypeNode node) {
        if (!node.getSuperTypes().isEmpty()) {
            writer.writeSpace();
            writer.writeSymbol(":");
            writer.writeSpace();
            writer.writeCommaSeparated(
                node.getSuperTypes(),
                this::writeTypeReference);
        }
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

    private void writeAll(Iterable<? extends Node> nodes) {
        for (Node node : nodes) {
            write(node);
        }
    }

    private void writeTypeReference(Type value) {
        value.accept(new Type.Visitor<Void>() {
            @Override
            public Void visit(ScalarType type) {
                if (isReservedTypeIdentifier(type)) {
                    writer.writeKeyword(type.getQualifiedName());
                } else {
                    writeQualifiedName(type.getQualifiedName());
                }
                return null;
            }

            @Override
            public Void visit(TypeParameter parameter) {
                writer.writeIdentifier(parameter.getName());
                return null;
            }

            @Override
            public Void visit(ParameterizedType type) {
                writeTypeReference(type.getRawType());
                writer.writeSymbol("<");
                writer.writeCommaSeparated(
                    type.getParameters(),
                    parameter -> writeTypeReference(parameter));
                writer.writeSymbol(">");
                return null;
            }

            @Override
            public Void visit(BoundTypeParameter type) {
                writeTypeReference(type.getValue());
                return null;
            }
        });
    }

    private static boolean isReservedTypeIdentifier(ScalarType type) {
        return RESERVED_TYPE_IDENTIFIERS.contains(type.getQualifiedName());
    }

    private void writeQualifiedName(String name) {
        writer.writeWithSeparator(
            asList(name.split("\\.")),
            writer::writeIdentifier,
            () -> {
                writer.writeSymbol(".");
            });
    }

    private void writeParenthesized(Node node, Node parent) {
        boolean requiresParens = precedence(parent) >= precedence(node);
        if (requiresParens) {
            writer.writeSymbol("(");
        }
        write(node);
        if (requiresParens) {
            writer.writeSymbol(")");
        }
    }

    private static int precedence(Node node) {
        return Precedence.OF.apply(node);
    }

    public static class Precedence {
        private final static Function<Node, Integer> OF =
            DynamicNodeMapper.instantiate(new Precedence(), "visit");

        public Integer visit(LiteralNode literal) {
            return Integer.MAX_VALUE;
        }

        public Integer visit(VariableReferenceNode variableReference) {
            return Integer.MAX_VALUE;
        }

        public Integer visit(ThisReferenceNode reference) {
            return Integer.MAX_VALUE;
        }

        public Integer visit(ArrayNode array) {
            return Integer.MAX_VALUE;
        }

        public Integer visit(AssignmentNode assignment) {
            return 1;
        }

        public Integer visit(TernaryConditionalNode ternaryConditional) {
            return 2;
        }

        public Integer visit(MethodCallNode methodCall) {
            return 15;
        }

        public Integer visit(ConstructorCallNode call) {
            return 13;
        }

        public Integer visit(OperationNode operation) {
            switch (operation.getOperator()) {
                case BOOLEAN_NOT:
                    return 14;
                case MULTIPLY:
                case DIVIDE:
                case MOD:
                    return 12;
                case ADD:
                case SUBTRACT:
                    return 11;
                case LESS_THAN:
                case LESS_THAN_OR_EQUAL:
                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                    return 9;
                case EQUALS:
                case NOT_EQUALS:
                    return 8;
                case BOOLEAN_AND:
                    return 4;
                case BOOLEAN_OR:
                    return 3;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        public Integer visit(InstanceOfNode instanceOf) {
            return 9;
        }

        public Integer visit(FieldAccessNode fieldAccess) {
            return 15;
        }

        public Integer visit(TypeCoercionNode typeCoercion) {
            return precedence(typeCoercion.getExpression());
        }

        public Integer visit(CastNode cast) {
            return 13;
        }

        public Integer visit(InstanceReceiver receiver) {
            return precedence(receiver.getExpression());
        }

        public Integer visit(StaticReceiver receiver) {
            return Integer.MAX_VALUE;
        }

        public Integer visit(Node node) {
            return 0;
        }
    }
}
