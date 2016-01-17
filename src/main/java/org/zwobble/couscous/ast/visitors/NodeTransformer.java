package org.zwobble.couscous.ast.visitors;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.zwobble.couscous.ast.*;

import java.util.List;
import java.util.Set;

public class NodeTransformer implements ExpressionNodeMapper<ExpressionNode>, StatementNodeMapper<StatementNode>, NodeMapper<Node> {
    // TODO: tidy up transform vs visit
    // TODO: don't expose specific statement/expression visit methods
    // (require passing through visit(ExpressionNode) and visit(StatementNode) for callers
    public TypeName transform(TypeName type) {
        return type;
    }

    public ExpressionNode visit(ExpressionNode value) {
        return value.accept((ExpressionNodeMapper<ExpressionNode>)this);
    }

    @Override
    public ExpressionNode visit(LiteralNode literal) {
        return literal.transform(this);
    }

    @Override
    public ExpressionNode visit(VariableReferenceNode variableReference) {
        return variableReference.transform(this);
    }

    @Override
    public ExpressionNode visit(ThisReferenceNode reference) {
        return reference.transform(this);
    }

    @Override
    public ExpressionNode visit(AssignmentNode assignment) {
        return assignment.transform(this);
    }

    @Override
    public ExpressionNode visit(TernaryConditionalNode ternaryConditional) {
        return ternaryConditional.transform(this);
    }

    @Override
    public ExpressionNode visit(MethodCallNode methodCall) {
        return methodCall.transform(this);
    }

    @Override
    public ExpressionNode visit(ConstructorCallNode call) {
        return call.transform(this);
    }

    @Override
    public ExpressionNode visit(FieldAccessNode fieldAccess) {
        return fieldAccess.transform(this);
    }

    @Override
    public ExpressionNode visit(TypeCoercionNode typeCoercion) {
        return typeCoercion.transform(this);
    }

    @Override
    public ExpressionNode visit(CastNode cast) {
        return cast.transform(this);
    }

    public Receiver visit(Receiver receiver) {
        // TODO: remove cast
        return (Receiver) receiver.accept(this);
    }

    @Override
    public Node visit(InstanceReceiver receiver) {
        return receiver.transform(this);
    }

    @Override
    public Node visit(StaticReceiver receiver) {
        return receiver.transform(this);
    }

    public VariableDeclaration transform(VariableDeclaration declaration) {
        return VariableDeclaration.var(declaration.getId(), declaration.getName(), transform(declaration.getType()));
    }

    public StatementNode visit(StatementNode statement) {
        return statement.accept((StatementNodeMapper<StatementNode>)this);
    }

    @Override
    public StatementNode visit(ReturnNode returnNode) {
        return returnNode.transform(this);
    }

    @Override
    public StatementNode visit(ExpressionStatementNode expressionStatement) {
        return expressionStatement.transform(this);
    }

    @Override
    public StatementNode visit(LocalVariableDeclarationNode localVariableDeclaration) {
        return localVariableDeclaration.transform(this);
    }

    @Override
    public StatementNode visit(IfStatementNode ifStatement) {
        return ifStatement.transform(this);
    }

    @Override
    public StatementNode visit(WhileNode whileLoop) {
        return whileLoop.transform(this);
    }

    @Override
    public FormalArgumentNode visit(FormalArgumentNode formalArgumentNode) {
        return formalArgumentNode.transform(this);
    }

    @Override
    public AnnotationNode visit(AnnotationNode annotation) {
        return annotation.transform(this);
    }

    @Override
    public MethodNode visit(MethodNode method) {
        return method.transform(this);
    }

    @Override
    public ConstructorNode visit(ConstructorNode constructor) {
        return constructor.transform(this);
    }

    @Override
    public FieldDeclarationNode visit(FieldDeclarationNode field) {
        return field.transform(this);
    }

    @Override
    public ClassNode visit(ClassNode classNode) {
        return classNode.transform(this);
    }

    public List<AnnotationNode> transformAnnotations(List<AnnotationNode> annotations) {
        return transformList(annotations, this::visit);
    }

    public Set<TypeName> transformTypes(Set<TypeName> superTypes) {
        return transformSet(superTypes, this::transform);
    }

    public List<FieldDeclarationNode> transformFields(List<FieldDeclarationNode> fields) {
        return transformList(fields, this::visit);
    }

    public List<MethodNode> transformMethods(List<MethodNode> methods) {
        return transformList(methods, this::visit);
    }

    public List<StatementNode> transformStatements(List<StatementNode> body) {
        return transformList(body, this::visit);
    }

    public List<ExpressionNode> transformExpressions(List<? extends ExpressionNode> expressions) {
        return transformList(expressions, this::visit);
    }

    public List<FormalArgumentNode> transformFormalArguments(List<FormalArgumentNode> arguments) {
        return transformList(arguments, this::visit);
    }

    private <T> Set<T> transformSet(Set<T> values, Function<T, T> function) {
        return ImmutableSet.copyOf(Iterables.transform(values, function));
    }

    private <T> List<T> transformList(List<? extends T> values, Function<T, T> function) {
        return ImmutableList.copyOf(Iterables.transform(values, function));
    }
}
