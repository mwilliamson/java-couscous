package org.zwobble.couscous.frontends.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.zwobble.couscous.ast.ClassNode;

import lombok.val;

public class JavaFrontend {
    public static List<ClassNode> readSourceDirectory(Path directoryPath) throws IOException {
        val reader = new JavaReader();
        
        return findJavaFiles(directoryPath)
            .map(javaFile -> reader.readClassFromFile(directoryPath, javaFile))
            .collect(Collectors.toList());
    }
    
    private static Stream<Path> findJavaFiles(Path directoryPath) throws IOException {
        return Files.walk(directoryPath)
            .filter(path -> path.toFile().isFile() && path.toString().endsWith(".java"));
    }
}
