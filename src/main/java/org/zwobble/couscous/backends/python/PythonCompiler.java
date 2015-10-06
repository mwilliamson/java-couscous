package org.zwobble.couscous.backends.python;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.ClassNode;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.PythonCodeGenerator.generateCode;
import static org.zwobble.couscous.backends.python.PythonSerializer.serialize;

import lombok.SneakyThrows;
import lombok.val;

public class PythonCompiler {
    private final Path root;

    public PythonCompiler(Path root) {
        this.root = root;
    }
    
    public void compile(List<ClassNode> classes) {
        for (val classNode : classes) {
            compileClass(classNode);
        }
        writeClass(TypeName.of("java.lang.Integer"),
            "class Integer(object):\n" +
            "    def parseInt(value):\n" +
            "        return int(value)"
        );
    }

    private Path pathForClass(TypeName className) {
        return root.resolve(className.getQualifiedName().replace(".", File.separator) + ".py");
    }

    private void compileClass(ClassNode classNode) {
        writeClass(classNode.getName(), serialize(generateCode(classNode)));
    }

    @SneakyThrows
    private void writeClass(TypeName name, String contents) {
        val path = pathForClass(name);
        Files.createDirectories(path.getParent());
        Files.write(path, asList(contents));
    }
}
