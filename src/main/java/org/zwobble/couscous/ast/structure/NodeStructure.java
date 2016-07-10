package org.zwobble.couscous.ast.structure;

import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.util.ExtraIterables;

import java.util.function.Predicate;
import java.util.stream.Stream;

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
        return ExtraIterables.stream(node.childNodes());
    }
}
