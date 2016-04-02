package org.zwobble.couscous.frontends.java;

import com.google.common.collect.Multimaps;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.ast.VariableNode;
import org.zwobble.couscous.util.FileSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.zwobble.couscous.ast.structure.NodeStructure.descendantNodesAndSelf;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.eagerFlatMap;
import static org.zwobble.couscous.util.ExtraStreams.toStream;

public class JavaFrontend {
    public List<TypeNode> readSourceDirectory(List<Path> sourcePaths, FileSet sourceFiles) throws IOException {
        List<Path> expandedSourceFiles = sourceFiles.filter(path -> path.toString().endsWith(".java")).files();
        List<TypeNode> classNodes = JavaReader.readClassesFromFiles(sourcePaths, expandedSourceFiles);
        ensureDeclarationsAreUnique(classNodes);
        return classNodes;
    }

    private void ensureDeclarationsAreUnique(List<TypeNode> classNodes) {
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