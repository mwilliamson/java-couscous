package org.zwobble.couscous.ast.visitors;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class NodeTransformer {
    public static NodeTransformer replaceExpressions(Map<ExpressionNode, ExpressionNode> replacements) {
        return builder().
            transformExpression(expression ->
                Optional.ofNullable(replacements.get(expression)))
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Node transform(Node node) {
        return node.accept(new NodeMapper<Node>() {
            @Override
            public Node visit(LiteralNode literal) {
                return transformExpression(literal);
            }

            @Override
            public Node visit(VariableReferenceNode variableReference) {
                return transformExpression(variableReference);
            }

            @Override
            public Node visit(ThisReferenceNode reference) {
                return transformExpression(reference);
            }

            @Override
            public Node visit(ArrayNode array) {
                return transformExpression(array);
            }

            @Override
            public Node visit(AssignmentNode assignment) {
                return transformExpression(assignment);
            }

            @Override
            public Node visit(TernaryConditionalNode ternaryConditional) {
                return transformExpression(ternaryConditional);
            }

            @Override
            public Node visit(MethodCallNode methodCall) {
                return transformExpression(methodCall);
            }

            @Override
            public Node visit(ConstructorCallNode call) {
                return transformExpression(call);
            }

            @Override
            public Node visit(OperationNode operation) {
                return transformExpression(operation);
            }

            @Override
            public Node visit(FieldAccessNode fieldAccess) {
                return transformExpression(fieldAccess);
            }

            @Override
            public Node visit(TypeCoercionNode typeCoercion) {
                return transformExpression(typeCoercion);
            }

            @Override
            public Node visit(CastNode cast) {
                return transformExpression(cast);
            }

            @Override
            public Node visit(InstanceReceiver receiver) {
                return transformReceiver(receiver);
            }

            @Override
            public Node visit(StaticReceiver receiver) {
                return transformReceiver(receiver);
            }

            @Override
            public Node visit(ReturnNode returnNode) {
                return transformStatement(returnNode);
            }

            @Override
            public Node visit(ExpressionStatementNode expressionStatement) {
                return transformStatement(expressionStatement);
            }

            @Override
            public Node visit(LocalVariableDeclarationNode localVariableDeclaration) {
                return transformStatement(localVariableDeclaration);
            }

            @Override
            public Node visit(IfStatementNode ifStatement) {
                return transformStatement(ifStatement);
            }

            @Override
            public Node visit(WhileNode whileLoop) {
                return transformStatement(whileLoop);
            }

            @Override
            public Node visit(FormalArgumentNode formalArgumentNode) {
                return transformFormalArgument(formalArgumentNode);
            }

            @Override
            public Node visit(AnnotationNode annotation) {
                return transformAnnotation(annotation);
            }

            @Override
            public Node visit(MethodNode methodNode) {
                return transformMethod(methodNode);
            }

            @Override
            public Node visit(ConstructorNode constructorNode) {
                return transformConstructor(constructorNode);
            }

            @Override
            public Node visit(FieldDeclarationNode declaration) {
                return transformField(declaration);
            }

            @Override
            public Node visit(ClassNode classNode) {
                return transformClass(classNode);
            }

            @Override
            public Node visit(InterfaceNode interfaceNode) {
                return transformInterface(interfaceNode);
            }
        });
    }

    public static class Builder {
        private Function<ExpressionNode, Optional<ExpressionNode>> transformExpression = expression -> Optional.empty();
        private Function<Type, Type> transformType = type -> type;
        private Function<MethodSignature, String> transformMethodName = MethodSignature::getName;

        public Builder transformExpression(
            Function<ExpressionNode, Optional<ExpressionNode>> transformExpression)
        {
            this.transformExpression = transformExpression;
            return this;
        }

        public Builder transformType(
            Function<Type, Type> transformType)
        {
            this.transformType = transformType;
            return this;
        }

        public Builder transformMethodName(Function<MethodSignature, String> transformMethodName) {
            this.transformMethodName = transformMethodName;
            return this;
        }

        public NodeTransformer build() {
            return new NodeTransformer(transformExpression, transformType, transformMethodName);
        }
    }

    private final Function<ExpressionNode, Optional<ExpressionNode>> transformExpression;
    private final Function<Type, Type> transformType;
    private final Function<MethodSignature, String> transformMethodName;

    private NodeTransformer(
        Function<ExpressionNode, Optional<ExpressionNode>> transformExpression,
        Function<Type, Type> transformType,
        Function<MethodSignature, String> transformMethodName)
    {
        this.transformExpression = transformExpression;
        this.transformType = transformType;
        this.transformMethodName = transformMethodName;
    }

    public MethodSignature transform(MethodSignature signature) {
        return new MethodSignature(
            transformMethodName(signature),
            eagerMap(signature.getArguments(), this::transform),
            transform(signature.getReturnType()));
    }

    public Type transform(Type type) {
        return transformType.apply(type);
    }

    public ScalarType transform(ScalarType type) {
        return (ScalarType)transformType.apply(type);
    }

    public String transformMethodName(MethodSignature signature) {
        return transformMethodName.apply(signature);
    }

    public ExpressionNode transformExpression(ExpressionNode expression) {
        return transformExpression.apply(expression)
            .orElseGet(() -> expression.transform(this));
    }

    public Receiver transformReceiver(Receiver receiver) {
        return receiver.transform(this);
    }

    public VariableDeclaration transform(VariableDeclaration declaration) {
        return VariableDeclaration.var(declaration.getId(), declaration.getName(), transform(declaration.getType()));
    }

    public final StatementNode transformStatement(StatementNode statement) {
        return statement.transform(this);
    }

    public FormalTypeParameterNode transformFormalTypeParameter(FormalTypeParameterNode typeParameter) {
        return typeParameter.transform(this);
    }

    public FormalArgumentNode transformFormalArgument(FormalArgumentNode formalArgumentNode) {
        return formalArgumentNode.transform(this);
    }

    public AnnotationNode transformAnnotation(AnnotationNode annotation) {
        return annotation.transform(this);
    }

    public MethodNode transformMethod(MethodNode method) {
        return method.transform(this);
    }

    public ConstructorNode transformConstructor(ConstructorNode constructor) {
        return constructor.transform(this);
    }

    public FieldDeclarationNode transformField(FieldDeclarationNode field) {
        return field.transform(this);
    }

    public ClassNode transformClass(ClassNode classNode) {
        return classNode.transform(this);
    }

    private Node transformInterface(InterfaceNode interfaceNode) {
        return interfaceNode.transform(this);
    }

    public List<AnnotationNode> transformAnnotations(List<AnnotationNode> annotations) {
        return transformList(annotations, this::transformAnnotation);
    }

    public Set<Type> transformTypes(Set<Type> superTypes) {
        return transformSet(superTypes, this::transform);
    }

    public List<FieldDeclarationNode> transformFields(List<FieldDeclarationNode> fields) {
        return transformList(fields, this::transformField);
    }

    public List<MethodNode> transformMethods(List<MethodNode> methods) {
        return transformList(methods, this::transformMethod);
    }

    public List<StatementNode> transformStatements(List<StatementNode> body) {
        return transformList(body, this::transformStatement);
    }

    public List<ExpressionNode> transformExpressions(List<? extends ExpressionNode> expressions) {
        return transformList(expressions, this::transformExpression);
    }

    public List<FormalTypeParameterNode> transformFormalTypeParameters(List<FormalTypeParameterNode> typeParameters) {
        return transformList(typeParameters, this::transformFormalTypeParameter);
    }

    public List<FormalArgumentNode> transformFormalArguments(List<FormalArgumentNode> arguments) {
        return transformList(arguments, this::transformFormalArgument);
    }

    private <T> Set<T> transformSet(Set<T> values, Function<T, T> function) {
        return ImmutableSet.copyOf(Iterables.transform(values, function));
    }

    private <T> List<T> transformList(List<? extends T> values, Function<T, T> function) {
        return ImmutableList.copyOf(Iterables.transform(values, function));
    }
}
