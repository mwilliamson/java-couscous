package org.zwobble.couscous.backends.python;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.PythonCodeGenerator.generateCode;
import static org.zwobble.couscous.backends.python.PythonSerializer.serialize;

import lombok.SneakyThrows;
import lombok.val;

public class PythonCompiler {
    public void compile(List<ClassNode> classes, Path path) {
        for (val classNode : classes) {
            compileClass(classNode, pathForClass(classNode, path));
        }
    }

    private Path pathForClass(ClassNode classNode, Path path) {
        return path.resolve(classNode.getName().replace(".", File.separator) + ".py");
    }

    @SneakyThrows
    private void compileClass(ClassNode classNode, Path path) {
        Files.createDirectories(path.getParent());
        Files.write(path, asList(serialize(generateCode(classNode))));
        
    }
}
