package org.zwobble.couscous;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.frontends.java.JavaFrontend;

public class CouscousCompiler {
    private final Backend backend;

    public CouscousCompiler(Backend backend) {
        this.backend = backend;
    }

    public void compileDirectory(Path path) throws IOException {
        List<ClassNode> classes = JavaFrontend.readSourceDirectory(path);
        backend.compile(classes);
    }

}
