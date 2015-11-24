package org.zwobble.couscous.frontends.java;

import com.google.common.collect.Sets;
import org.zwobble.couscous.ast.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zwobble.couscous.ast.structure.NodeStructure.descendantNodesAndSelf;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraStreams.toStream;

public class FreeVariables {
    public static List<VariableDeclaration> findFreeVariables(List<FormalArgumentNode> formalArguments, List<? extends Node> body) {
        Stream<VariableDeclaration> referencedDeclarations = body.stream()
            .flatMap(FreeVariables::findReferencedDeclarations);
        Stream<VariableDeclaration> declarations = Stream.concat(
            formalArguments.stream()
                .map(argument -> argument.getDeclaration()),
            body.stream().flatMap(FreeVariables::findDeclarations));
        return Sets.difference(
            referencedDeclarations.collect(Collectors.toSet()),
            declarations.collect(Collectors.toSet())).stream().collect(Collectors.toList());
    }

    private static Stream<VariableDeclaration> findReferencedDeclarations(Node root) {
        Stream<VariableReferenceNode> references = descendantNodesAndSelf(root).flatMap(node ->
            toStream(tryCast(VariableReferenceNode.class, node)));
        return references.map(VariableReferenceNode::getReferent);
    }

    private static Stream<VariableDeclaration> findDeclarations(Node root) {
        Stream<VariableNode> declarations = descendantNodesAndSelf(root).flatMap(node ->
            toStream(tryCast(VariableNode.class, node)));
        return declarations.map(VariableNode::getDeclaration);
    }
}
