package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zwobble.couscous.ast.structure.NodeStructure.descendantNodesAndSelf;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraStreams.toStream;

public class FreeVariables {
    public static Set<TypeParameter> findFreeTypeParameters(Node node) {
        // TODO: this assumes that *all* type parameters are free, which is often but not always true in inner types
        Set<TypeParameter> freeTypeParameters = new HashSet<>();

        NodeTransformer.builder().transformType(type -> {
            type.accept(new Type.Visitor<Void>() {
                @Override
                public Void visit(ScalarType type) {
                    return null;
                }

                @Override
                public Void visit(TypeParameter parameter) {
                    freeTypeParameters.add(parameter);
                    return null;
                }

                @Override
                public Void visit(ParameterizedType type) {
                    type.getParameters().forEach(parameter -> parameter.accept(this));
                    return null;
                }

                @Override
                public Void visit(BoundTypeParameter type) {
                    type.getValue().accept(this);
                    return null;
                }
            });
            return type;
        }).build().transform(node);

        return freeTypeParameters;
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
        return declarations.map(VariableNode::getDeclaration);
    }
}
