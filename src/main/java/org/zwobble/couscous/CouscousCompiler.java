package org.zwobble.couscous;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.frontends.java.JavaFrontend;

public class CouscousCompiler {
    private final JavaFrontend frontend;
    private final Backend backend;

    public CouscousCompiler(JavaFrontend frontend, Backend backend) {
        this.frontend = frontend;
        this.backend = backend;
    }

    public void compileDirectory(List<Path> sourcePaths, Path path) throws IOException {
        List<ClassNode> classes = frontend.readSourceDirectory(sourcePaths, path);
        backend.compile(classes);
    }

}
