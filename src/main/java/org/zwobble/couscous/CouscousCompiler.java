package org.zwobble.couscous;

import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.frontends.java.JavaFrontend;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.list;

public class CouscousCompiler {
    private final JavaFrontend frontend;
    private final Backend backend;

    public CouscousCompiler(JavaFrontend frontend, Backend backend) {
        this.frontend = frontend;
        this.backend = backend;
    }

    public void compileDirectory(List<Path> sourcePaths, Path path) throws IOException {
        List<TypeNode> classes = frontend.readSourceDirectory(sourcePaths, list(path));
        backend.compile(classes);
    }

}
