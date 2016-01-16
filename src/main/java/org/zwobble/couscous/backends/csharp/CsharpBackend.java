package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

public class CsharpBackend implements Backend {
    private static final List<String> RUNTIME_FILES = ImmutableList.of(
        "java/lang/String.cs");

    private final Path directoryPath;
    private final String namespace;

    public CsharpBackend(Path directoryPath, String namespace) {
        this.directoryPath = directoryPath;
        this.namespace = namespace;
    }

    @Override
    public void compile(List<ClassNode> classes) throws IOException {
        Files.write(
            directoryPath.resolve("Program.cs"),
            Iterables.concat(
                transform(
                    classes,
                    classNode -> compileClass(classNode)),
                transform(
                    RUNTIME_FILES,
                    runtimeFile -> readRuntimeFile(runtimeFile))));
    }

    private String readRuntimeFile(String runtimeFile) {
        try {
            return Resources.toString(Resources.getResource("csharp/runtime/" + runtimeFile), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private String compileClass(ClassNode classNode) {
        String namespace = this.namespace +
            classNode.getName().getPackage()
                .map(packageName -> packageName + ".")
                .orElse("");
        return "namespace " + namespace + " {" +
            "    internal class " + classNode.getSimpleName() + " {" +
            String.join("\n", transform(classNode.getMethods(), method -> compileMethod(method))) +
            "    }" +
            "}";
    }

    private String compileMethod(MethodNode method) {
        ReturnNode returnNode = (ReturnNode) method.getBody().get(0);
        return
            "public static dynamic " + method.getName() + "() {" +
            "    return " + CsharpSerializer.serialize(returnNode.getValue(), namespace) + ";" +
            "}";
    }
}
