package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zwobble.couscous.ast.structure.NodeStructure.descendantNodesAndSelf;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraStreams.toStream;

public class FreeVariables {
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
