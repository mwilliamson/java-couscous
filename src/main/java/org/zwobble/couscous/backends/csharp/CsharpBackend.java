package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.TypeNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

public class CsharpBackend implements Backend {
    private static final List<String> RUNTIME_FILES = ImmutableList.of(
        "java/lang/Integer.cs",
        "java/lang/Object.cs",
        "java/util/Arrays.cs",
        // TODO: implement Iterable/Iterator in Java
        "java/lang/Iterable.cs",
        "java/util/Iterator.cs");

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
            Iterables.<String>concat(
                transform(
                    classes,
                    classNode -> compileClass(classNode)),
                transform(
                    RUNTIME_FILES,
                    runtimeFile -> readRuntimeFile(runtimeFile))));
    }

    private String readRuntimeFile(String runtimeFile) {
        try {
            return Resources.toString(Resources.getResource("csharp/runtime/" + runtimeFile), StandardCharsets.UTF_8)
                .replace("namespace Couscous", "namespace " + namespace);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private String compileClass(TypeNode classNode) {
        return CsharpSerializer.serialize(CsharpCodeGenerator.generateCode(classNode, namespace));
    }
}
