package org.zwobble.couscous.backends.csharp;

import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.values.PrimitiveValueVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

public class CsharpBackend implements Backend {
    private final Path directoryPath;
    private final String namespace;

    public CsharpBackend(Path directoryPath, String namespace) {
        this.directoryPath = directoryPath;
        this.namespace = namespace;
    }

    @Override
    public void compile(List<ClassNode> classes) throws IOException {
        Files.write(
            directoryPath.resolve("Program.cs"),
            transform(
                classes,
                classNode -> compileClass(classNode)
        ));
    }

    private String compileClass(ClassNode classNode) {
        String namespace = this.namespace +
            classNode.getName().getPackage()
                .map(packageName -> packageName + ".")
                .orElse("");
        return "namespace " + namespace + " {" +
            "    public class " + classNode.getSimpleName() + " {" +
            String.join("\n", transform(classNode.getMethods(), method -> compileMethod(method))) +
            "    }" +
            "}";
    }

    private String compileMethod(MethodNode method) {
        ReturnNode returnNode = (ReturnNode) method.getBody().get(0);
        return
            "public static string " + method.getName() + "() {" +
            "    return " + compileExpression(returnNode.getValue()) + ";" +
            "}";
    }

    private String compileExpression(ExpressionNode value) {
        return value.accept(new ExpressionNodeMapper<String>() {
            @Override
            public String visit(LiteralNode literal) {
                return literal.getValue().accept(new PrimitiveValueVisitor<String>() {
                    @Override
                    public String visitInteger(int value) {
                        return Integer.toString(value);
                    }

                    @Override
                    public String visitString(String value) {
                        // TODO: escaping
                        return "\"" + value + "\"";
                    }

                    @Override
                    public String visitBoolean(boolean value) {
                        return value ? "true" : "false";
                    }

                    @Override
                    public String visitUnit() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public String visitType(TypeName value) {
                        throw new UnsupportedOperationException();
                    }
                });
            }

            @Override
            public String visit(VariableReferenceNode variableReference) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(ThisReferenceNode reference) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(AssignmentNode assignment) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(TernaryConditionalNode ternaryConditional) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(MethodCallNode methodCall) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(ConstructorCallNode call) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(FieldAccessNode fieldAccess) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(TypeCoercionNode typeCoercion) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(CastNode cast) {
                throw new UnsupportedOperationException();
            }
        });
    }
}
