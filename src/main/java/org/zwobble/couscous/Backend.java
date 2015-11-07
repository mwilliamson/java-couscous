package org.zwobble.couscous;

import java.io.IOException;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;

public interface Backend {
    void compile(List<ClassNode> classes) throws IOException;
}
