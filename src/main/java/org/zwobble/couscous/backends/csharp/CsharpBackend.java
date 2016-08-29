package org.zwobble.couscous.backends.csharp;

import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.TypeNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

public class CsharpBackend implements Backend {
    private final Path outputFilePath;
    private final String namespace;

    public CsharpBackend(Path outputFilePath, String namespace) {
        this.outputFilePath = outputFilePath;
        this.namespace = namespace;
    }

    @Override
    public void compile(List<TypeNode> classes) throws IOException {
        outputFilePath.getParent().toFile().mkdirs();
        Files.write(
            outputFilePath,
            transform(
                CsharpCodeGenerator.generateCode(classes, namespace),
                CsharpSerializer::serialize));
    }

}
