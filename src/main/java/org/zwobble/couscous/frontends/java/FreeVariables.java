package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableSet;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.DynamicNodeMapper;
import org.zwobble.couscous.types.*;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zwobble.couscous.ast.structure.NodeStructure.descendantNodesAndSelf;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.iterable;
import static org.zwobble.couscous.util.ExtraStreams.toStream;

public class FreeVariables {
    public static class FindReferencedTypes {
        private static final Function<Node, Stream<Type>> VISITOR =
            DynamicNodeMapper.instantiate(new FindReferencedTypes(), "visit");

        public Stream<Type> visit(Node node) {
            return Stream.empty();
        }

        public Stream<Type> visit(FieldDeclarationNode declaration) {
            return Stream.of(declaration.getType());
        }

        public Stream<Type> visit(LocalVariableDeclarationNode localVariableDeclaration) {
            return Stream.of(localVariableDeclaration.getType());
        }

        public Stream<Type> visit(MethodNode methodNode) {
            return Stream.concat(
                methodNode.getArguments().stream().map(argument -> argument.getType()),
                Stream.of(methodNode.getReturnType()));
        }

        public Stream<Type> visit(ConstructorCallNode call) {
            return Stream.of(call.getType());
        }

        public Stream<Type> visit(MethodCallNode methodCall) {
            return methodCall.getTypeParameters().stream();
        }
    }

    public static class FindDeclaredTypes {
        private static final Function<Node, Stream<TypeParameter>> VISITOR =
            DynamicNodeMapper.instantiate(new FindDeclaredTypes(), "visit");

        public Stream<TypeParameter> visit(Node node) {
            return Stream.empty();
        }

        public Stream<TypeParameter> visit(MethodNode methodNode) {
            return methodNode.getTypeParameters().stream().map(parameter -> parameter.getType());
        }
    }

    public static Set<TypeParameter> findFreeTypeParameters(Node root) {
        // TODO: test removal of declared types
        Set<TypeParameter> declaredTypes = descendantNodesAndSelf(root)
            .flatMap(FindDeclaredTypes.VISITOR)
            .collect(Collectors.toSet());

        // TODO: this assumes that *all* type parameters are free, which is often but not always true in inner types
        Stream<TypeParameter> types = descendantNodesAndSelf(root)
            .flatMap(FindReferencedTypes.VISITOR)
            .flatMap(type -> type.accept(new Type.Visitor<Stream<TypeParameter>>() {
                @Override
                public Stream<TypeParameter> visit(ScalarType type) {
                    return Stream.empty();
                }

                @Override
                public Stream<TypeParameter> visit(TypeParameter parameter) {
                    return Stream.of(parameter);
                }

                @Override
                public Stream<TypeParameter> visit(ParameterizedType type) {
                    return type.getParameters().stream().flatMap(parameter -> parameter.accept(this));
                }

                @Override
                public Stream<TypeParameter> visit(BoundTypeParameter type) {
                    return type.getValue().accept(this);
                }
            }))
            .filter(type -> !declaredTypes.contains(type));

        return ImmutableSet.copyOf(iterable(() -> types));
    }

    public static List<ReferenceNode> findFreeVariables(List<? extends Node> body) {
        Set<VariableDeclaration> declarations = body.stream()
            .flatMap(FreeVariables::findDeclarations)
            .collect(Collectors.toSet());

        return body.stream()
            .flatMap(FreeVariables::findReferences)
            .filter(reference ->
                tryCast(VariableReferenceNode.class, reference)
                    .map(variableReference -> !declarations.contains(variableReference.getReferent()))
                    .orElse(true))
            .collect(Collectors.toList());
    }

    private static Stream<ReferenceNode> findReferences(Node root) {
        return descendantNodesAndSelf(root).flatMap(node ->
            toStream(tryCast(ReferenceNode.class, node)));
    }

    private static Stream<VariableDeclaration> findDeclarations(Node root) {
        Stream<VariableNode> declarations = descendantNodesAndSelf(root).flatMap(node ->
            toStream(tryCast(VariableNode.class, node)));
        Stream<TryNode> tryStatements = descendantNodesAndSelf(root).flatMap(node -> toStream(tryCast(TryNode.class, node)));
        Stream<ExceptionHandlerNode> handlers = tryStatements.flatMap(statement -> statement.getExceptionHandlers().stream());
        return Stream.concat(
            declarations.map(VariableNode::getDeclaration),
            handlers.map(ExceptionHandlerNode::getDeclaration));
    }
}
