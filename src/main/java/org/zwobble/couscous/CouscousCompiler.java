package org.zwobble.couscous;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;

public class CouscousCompiler {
    private final Frontend frontend;
    private final Backend backend;

    public CouscousCompiler(Frontend frontend, Backend backend) {
        this.frontend = frontend;
        this.backend = backend;
    }

    public void compileDirectory(Path path) throws IOException {
        List<ClassNode> classes = frontend.readSourceDirectory(path);
        backend.compile(classes);
    }

}
