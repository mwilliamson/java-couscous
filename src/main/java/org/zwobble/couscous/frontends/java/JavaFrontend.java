package org.zwobble.couscous.frontends.java;

import com.google.common.collect.Multimaps;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.ast.VariableNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zwobble.couscous.ast.structure.NodeStructure.descendantNodesAndSelf;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.eagerFlatMap;
import static org.zwobble.couscous.util.ExtraLists.flatMap;
import static org.zwobble.couscous.util.ExtraStreams.toStream;

public class JavaFrontend {
    public List<ClassNode> readSourceDirectory(List<Path> sourcePaths, Path directoryPath) throws IOException {
        List<ClassNode> classNodes = flatMap(
            findJavaFiles(directoryPath),
            javaFile -> JavaReader.readClassFromFile(sourcePaths, javaFile));
        ensureDeclarationsAreUnique(classNodes);
        return classNodes;
    }

    private Stream<Path> findJavaFiles(Path directoryPath) throws IOException {
        return Files.walk(directoryPath)
            .filter(path -> path.toFile().isFile() && path.toString().endsWith(".java"));
    }

    private void ensureDeclarationsAreUnique(List<ClassNode> classNodes) {
        List<VariableDeclaration> declarations = eagerFlatMap(classNodes, this::findDeclarations);
        Multimaps.index(declarations, VariableDeclaration::getId)
            .asMap()
            .forEach((id, declarationsWithId) -> {
                if (declarationsWithId.size() > 1) {
                    throw new RuntimeException("Declaration " + id + " is declared " + declarationsWithId.size() + " times");
                }
            });
    }

    private List<VariableDeclaration> findDeclarations(Node root) {
        return descendantNodesAndSelf(root)
            .flatMap(node -> toStream(tryCast(VariableNode.class, node)
                .map(VariableNode::getDeclaration)))
            .collect(Collectors.toList());
    }
}