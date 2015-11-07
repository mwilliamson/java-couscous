package org.zwobble.couscous;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;

public interface Frontend {
    List<ClassNode> readSourceDirectory(Path path) throws IOException;
}
