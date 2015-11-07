package org.zwobble.couscous.backends.python;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;

import com.google.common.io.Resources;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.PythonCodeGenerator.generateCode;
import static org.zwobble.couscous.backends.python.PythonSerializer.serialize;

public class PythonBackend {
    private static final List<String> RUNTIME_FILES = asList(
        "java.lang.Object",
        "java.lang.Integer",
        "_couscous");
    
    private final Path root;
    private final String packageName;
    
    public PythonBackend(Path root, String packageName) {
        this.root = root;
        this.packageName = packageName;
    }
    
    public void compile(List<ClassNode> classes) throws IOException {
        for (ClassNode classNode : classes) {
            compileClass(classNode);
        }
        for (String runtimeFile : RUNTIME_FILES) {
            String path = relativePathForModule(runtimeFile);
            writeModule(
                runtimeFile,
                Resources.toString(Resources.getResource("python/runtime/" + path), StandardCharsets.UTF_8));
        }
    }
    
    private Path destinationPathForModule(String moduleName) {
        return root.resolve(packageName).resolve(relativePathForModule(moduleName));
    }
    
    private String relativePathForModule(String moduleName) {
        return moduleName.replace(".", File.separator) + ".py";
    }
    
    private void compileClass(ClassNode classNode) throws IOException {
        writeModule(
            classNode.getName().getQualifiedName(),
            serialize(generateCode(classNode)));
    }
    
    private void writeModule(String name, String contents) throws IOException {
        Path path = destinationPathForModule(name);
        Files.createDirectories(path.getParent());
        createPythonPackages(path.getParent());
        Files.write(path, asList(contents));
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