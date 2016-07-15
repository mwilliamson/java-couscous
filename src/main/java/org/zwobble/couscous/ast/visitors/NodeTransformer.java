package org.zwobble.couscous.ast.visitors;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.TypeParameter;
import org.zwobble.couscous.util.ExtraLists;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.zwobble.couscous.util.ExtraLists.*;

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

    public static class Builder {
        private Function<StatementNode, Optional<List<StatementNode>>> transformStatement = statement -> Optional.empty();
        private Function<ExpressionNode, Optional<ExpressionNode>> transformExpression = expression -> Optional.empty();
        private Function<Type, Type> transformType = type -> type;
        private Function<MethodSignature, String> transformMethodName = MethodSignature::getName;
        private Function<String, String> transformFieldName = name -> name;

        public Builder transformExpression(
            Function<ExpressionNode, Optional<ExpressionNode>> transformExpression)
        {
            this.transformExpression = transformExpression;
            return this;
        }

        public Builder transformStatement(
            Function<StatementNode, Optional<List<StatementNode>>> transformStatement)
        {
            this.transformStatement = transformStatement;
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

        public Builder transformFieldName(Function<String, String> transformFieldName) {
            this.transformFieldName = transformFieldName;
            return this;
        }

        public NodeTransformer build() {
            return new NodeTransformer(
                transformExpression,
                transformStatement,
                transformType,
                transformMethodName,
                transformFieldName
            );
        }
    }

    private final Function<ExpressionNode, Optional<ExpressionNode>> transformExpression;
    private final Function<StatementNode, Optional<List<StatementNode>>> transformStatement;
    private final Function<Type, Type> transformType;
    private final Function<MethodSignature, String> transformMethodName;
    private final Function<String, String> transformFieldName;

    private NodeTransformer(
        Function<ExpressionNode, Optional<ExpressionNode>> transformExpression,
        Function<StatementNode, Optional<List<StatementNode>>> transformStatement,
        Function<Type, Type> transformType,
        Function<MethodSignature, String> transformMethodName,
        Function<String, String> transformFieldName)
    {
        this.transformExpression = transformExpression;
        this.transformStatement = transformStatement;
        this.transformType = transformType;
        this.transformMethodName = transformMethodName;
        this.transformFieldName = transformFieldName;
    }

    public Node transform(Node node) {
        if (node instanceof ExpressionNode) {
            return transformExpression((ExpressionNode) node);
        } else if (node instanceof StatementNode) {
            List<StatementNode> statements = ExtraLists.copyOf(transformStatement((StatementNode) node));
            if (statements.size() != 1) {
                throw new UnsupportedOperationException("TODO: add support for compound statements");
            }
            return statements.get(0);
        } else if (node instanceof TypeNode) {
            return transformTypeDeclaration((TypeNode) node);
        } else {
            return node.transformSubtree(this);
        }
    }

    public MethodSignature transform(MethodSignature signature) {
        return new MethodSignature(
            transformMethodName(signature),
            eagerMap(signature.getTypeParameters(), this::transform),
            eagerMap(signature.getArguments(), this::transform),
            transform(signature.getReturnType()));
    }

    public Type transform(Type type) {
        return transformType.apply(type);
    }

    public ScalarType transform(ScalarType type) {
        return (ScalarType)transformType.apply(type);
    }

    public TypeParameter transform(TypeParameter type) {
        return (TypeParameter)transformType.apply(type);
    }

    public String transformMethodName(MethodSignature signature) {
        return transformMethodName.apply(signature);
    }

    public String transformFieldName(String name) {
        return transformFieldName.apply(name);
    }

    public ExpressionNode transformExpression(ExpressionNode expression) {
        ExpressionNode transformed = expression.transformSubtree(this);
        return transformExpression.apply(transformed).orElse(transformed);
    }

    public Receiver transformReceiver(Receiver receiver) {
        return receiver.transformSubtree(this);
    }

    public VariableDeclaration transform(VariableDeclaration declaration) {
        return VariableDeclaration.var(declaration.getId(), declaration.getName(), transform(declaration.getType()));
    }

    public final Iterable<StatementNode> transformStatement(StatementNode statement) {
        StatementNode transformed = statement.transformSubtree(this);
        return transformStatement.apply(transformed).orElseGet(() -> list(transformed));
    }

    public FormalTypeParameterNode transformFormalTypeParameter(FormalTypeParameterNode typeParameter) {
        return typeParameter.transformSubtree(this);
    }

    public FormalArgumentNode transformFormalArgument(FormalArgumentNode formalArgumentNode) {
        return formalArgumentNode.transformSubtree(this);
    }

    public AnnotationNode transformAnnotation(AnnotationNode annotation) {
        return annotation.transformSubtree(this);
    }

    public MethodNode transformMethod(MethodNode method) {
        return method.transformSubtree(this);
    }

    public ConstructorNode transformConstructor(ConstructorNode constructor) {
        return constructor.transformSubtree(this);
    }

    public FieldDeclarationNode transformField(FieldDeclarationNode field) {
        return field.transformSubtree(this);
    }

    public TypeNode transformTypeDeclaration(TypeNode typeNode) {
        return typeNode.transformSubtree(this);
    }

    public ClassNode transformClass(ClassNode classNode) {
        return classNode.transformSubtree(this);
    }

    private Node transformInterface(InterfaceNode interfaceNode) {
        return interfaceNode.transformSubtree(this);
    }

    private Node transformEnum(EnumNode enumNode) {
        return enumNode.transformSubtree(this);
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
        return eagerFlatMap(body, this::transformStatement);
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
