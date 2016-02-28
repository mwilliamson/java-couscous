package org.zwobble.couscous.ast.structure;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeMapper;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.zwobble.couscous.util.ExtraStreams.concatStreams;

public class NodeStructure {
    public static Stream<? extends Node> descendantNodesAndSelf(Node node, Predicate<Node> predicate) {
        return Stream.concat(Stream.of(node), descendantNodes(node, predicate));
    }

    public static Stream<? extends Node> descendantNodes(Node node, Predicate<Node> predicate) {
        return childNodes(node)
            .filter(predicate)
            .flatMap(child -> descendantNodesAndSelf(child, predicate));
    }

    public static Stream<? extends Node> descendantNodesAndSelf(Node node) {
        return Stream.concat(Stream.of(node), descendantNodes(node));
    }

    public static Stream<? extends Node> descendantNodes(Node node) {
        return childNodes(node).flatMap(NodeStructure::descendantNodesAndSelf);
    }

    public static Stream<? extends Node> childNodes(Node node) {
        // TODO: is this just duplicating the logic already in node.transform?
        return node.accept(new NodeMapper<Stream<? extends Node>>() {
            @Override
            public Stream<Node> visit(LiteralNode literal) {
                return Stream.empty();
            }

            @Override
            public Stream<Node> visit(VariableReferenceNode variableReference) {
                return Stream.empty();
            }

            @Override
            public Stream<Node> visit(ThisReferenceNode reference) {
                return Stream.empty();
            }

            @Override
            public Stream<Node> visit(AssignmentNode assignment) {
                return Stream.of(assignment.getValue(), assignment.getTarget());
            }

            @Override
            public Stream<Node> visit(TernaryConditionalNode ternaryConditional) {
                return Stream.of(
                    ternaryConditional.getCondition(),
                    ternaryConditional.getIfTrue(),
                    ternaryConditional.getIfFalse());
            }

            @Override
            public Stream<Node> visit(MethodCallNode methodCall) {
                return Stream.concat(
                    Stream.of(methodCall.getReceiver()),
                    methodCall.getArguments().stream());
            }

            @Override
            public Stream<? extends ExpressionNode> visit(ConstructorCallNode call) {
                return call.getArguments().stream();
            }

            @Override
            public Stream<? extends Node> visit(OperationNode operation) {
                return operation.getArguments().stream();
            }

            @Override
            public Stream<Node> visit(FieldAccessNode fieldAccess) {
                return Stream.of(fieldAccess.getLeft());
            }

            @Override
            public Stream<? extends Node> visit(TypeCoercionNode typeCoercion) {
                return Stream.of(typeCoercion.getExpression());
            }

            @Override
            public Stream<? extends Node> visit(CastNode cast) {
                return Stream.of(cast.getExpression());
            }

            @Override
            public Stream<? extends Node> visit(InstanceReceiver receiver) {
                return Stream.of(receiver.getExpression());
            }

            @Override
            public Stream<? extends Node> visit(StaticReceiver receiver) {
                return Stream.empty();
            }

            @Override
            public Stream<Node> visit(ReturnNode returnNode) {
                return Stream.of(returnNode.getValue());
            }

            @Override
            public Stream<Node> visit(ExpressionStatementNode expressionStatement) {
                return Stream.of(expressionStatement.getExpression());
            }

            @Override
            public Stream<Node> visit(LocalVariableDeclarationNode localVariableDeclaration) {
                return Stream.of(localVariableDeclaration.getInitialValue());
            }

            @Override
            public Stream<Node> visit(IfStatementNode ifStatement) {
                return concatStreams(
                    Stream.of(ifStatement.getCondition()),
                    ifStatement.getTrueBranch().stream(),
                    ifStatement.getFalseBranch().stream());
            }

            @Override
            public Stream<Node> visit(WhileNode whileLoop) {
                return Stream.concat(
                    Stream.of(whileLoop.getCondition()),
                    whileLoop.getBody().stream());
            }

            @Override
            public Stream<? extends Node> visit(FormalArgumentNode formalArgumentNode) {
                return Stream.empty();
            }

            @Override
            public Stream<? extends Node> visit(AnnotationNode annotation) {
                return Stream.empty();
            }

            @Override
            public Stream<Node> visit(ClassNode classNode) {
                return concatStreams(
                    Stream.of(classNode.getConstructor()),
                    classNode.getFields().stream(),
                    classNode.getMethods().stream());
            }

            @Override
            public Stream<? extends Node> visit(InterfaceNode interfaceNode) {
                return interfaceNode.getMethods().stream();
            }

            @Override
            public Stream<Node> visit(MethodNode methodNode) {
                return concatStreams(
                    methodNode.getAnnotations().stream(),
                    methodNode.getArguments().stream(),
                    methodNode.getBody().stream());
            }

            @Override
            public Stream<Node> visit(ConstructorNode constructorNode) {
                return Stream.concat(constructorNode.getArguments().stream(), constructorNode.getBody().stream());
            }

            @Override
            public Stream<? extends Node> visit(FieldDeclarationNode declaration) {
                return Stream.empty();
            }
        });
    }
}
