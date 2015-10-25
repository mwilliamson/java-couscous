package org.zwobble.couscous.frontends.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.util.ExtraLists;

public class JavaFrontend {
    public static List<ClassNode> readSourceDirectory(Path directoryPath) throws IOException {
        JavaReader reader = new JavaReader();
        return ExtraLists.map(
            findJavaFiles(directoryPath),
            javaFile -> reader.readClassFromFile(directoryPath, javaFile));
    }
    
    private static Stream<Path> findJavaFiles(Path directoryPath) throws IOException {
        return Files.walk(directoryPath)
            .filter(path -> path.toFile().isFile() && path.toString().endsWith(".java"));
    }
}