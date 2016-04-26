package org.zwobble.couscous.tests.backends.csharp;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.backends.csharp.CsharpBackend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

public class CsharpTestBackend implements Backend {
    private static final List<String> RUNTIME_FILES = ImmutableList.of(
        "java/lang/Integer.cs",
        "java/lang/Object.cs",
        "java/util/Arrays.cs",
        // TODO: implement Iterable/Iterator in Java
        "java/lang/Iterable.cs",
        "java/util/Iterator.cs");

    private final Path outputDirectory;
    private final String namespace;

    public CsharpTestBackend(Path outputDirectory, String namespace) {
        this.outputDirectory = outputDirectory;
        this.namespace = namespace;
    }

    @Override
    public void compile(List<TypeNode> classes) throws IOException {
        new CsharpBackend(outputDirectory.resolve("Program.cs"), namespace).compile(classes);
        Files.write(
            outputDirectory.resolve("Runtime.cs"),
            transform(
                RUNTIME_FILES,
                runtimeFile -> readRuntimeFile(runtimeFile)));
    }

    private String readRuntimeFile(String runtimeFile) {
        try {
            return Resources.toString(Resources.getResource("org/zwobble/couscous/tests/backends/csharp/runtime/" + runtimeFile), StandardCharsets.UTF_8)
                .replace("namespace Couscous", "namespace " + namespace);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
