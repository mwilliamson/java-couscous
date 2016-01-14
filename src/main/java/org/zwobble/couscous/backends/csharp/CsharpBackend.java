package org.zwobble.couscous.backends.csharp;

import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.list;

public class CsharpBackend implements Backend {
    private final Path directoryPath;
    private final String namespace;

    public CsharpBackend(Path directoryPath, String namespace) {
        this.directoryPath = directoryPath;
        this.namespace = namespace;
    }

    @Override
    public void compile(List<ClassNode> classes) throws IOException {
        Files.write(directoryPath.resolve("Program.cs"), list(
            "namespace " + namespace + "{" +
            "    class Program {" +
            "        public static string run() {" +
            "            return \"T\";" +
            "        }" +
            "    }" +
            "}"
        ));
    }
}
