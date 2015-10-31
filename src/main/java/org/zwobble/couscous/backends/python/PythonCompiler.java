package org.zwobble.couscous.backends.python;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.ClassNode;
import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.PythonCodeGenerator.generateCode;
import static org.zwobble.couscous.backends.python.PythonSerializer.serialize;

public class PythonCompiler {
    private final Path root;
    private final String packageName;
    
    public PythonCompiler(Path root, String packageName) {
        this.root = root;
        this.packageName = packageName;
    }
    
    public void compile(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            compileClass(classNode);
        }
        writeClass(TypeName.of("java.lang.Integer"), "class Integer(object):\n    def parseInt(value):\n        return int(value)");
        writeClass(TypeName.of("_couscous"),
            "def _div_round_to_zero(a, b): return -(-a // b) if (a < 0) ^ (b < 0) else a // b\n" +
            "def _mod_round_to_zero(a, b): return -(-a % b) if (a < 0) ^ (b < 0) else a % b");
    }
    
    private Path pathForClass(TypeName className) {
        return root.resolve(packageName)
            .resolve(className.getQualifiedName().replace(".", File.separator) + ".py");
    }
    
    private void compileClass(ClassNode classNode) {
        writeClass(classNode.getName(), serialize(generateCode(classNode)));
    }
    
    private void writeClass(TypeName name, String contents) {
        try {
            final java.nio.file.Path path = pathForClass(name);
            Files.createDirectories(path.getParent());
            createPythonPackages(path.getParent());
            Files.write(path, asList(contents));
        } catch (final java.lang.Throwable $ex) {
            throw new RuntimeException($ex);
        }
    }
    
    private void createPythonPackages(Path packagePath) throws IOException {
        while (packagePath.startsWith(root)) {
            final java.nio.file.Path packageFile = packagePath.resolve("__init__.py");
            if (!packageFile.toFile().exists()) {
                Files.createFile(packageFile);
            }
            packagePath = packagePath.getParent();
        }
    }
}