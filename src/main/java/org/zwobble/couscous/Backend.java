package org.zwobble.couscous;

import org.zwobble.couscous.ast.TypeNode;

import java.io.IOException;
import java.util.List;

public interface Backend {
    void compile(List<TypeNode> classes) throws IOException;
}
