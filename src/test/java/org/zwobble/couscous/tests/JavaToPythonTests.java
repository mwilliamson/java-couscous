package org.zwobble.couscous.tests;

import org.zwobble.couscous.CouscousCompiler;
import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.backends.python.PythonBackend;
import org.zwobble.couscous.frontends.java.JavaFrontend;
import org.zwobble.couscous.tests.backends.python.PythonMethodRunner;
import org.zwobble.couscous.values.PrimitiveValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaToPythonTests extends CompilerTests {
    protected PrimitiveValue execProgram(
            Path directory,
            ScalarType type,
            String methodName,
            List<PrimitiveValue> arguments,
            ScalarType returnType) throws IOException, InterruptedException {

        Path directoryPath = Files.createTempDirectory(null);
        try {
            CouscousCompiler compiler = new CouscousCompiler(
                new JavaFrontend(),
                new PythonBackend(directoryPath, "couscous"));
            compiler.compileDirectory(list(directory), directory);
            return PythonMethodRunner.runFunction(directoryPath, type, methodName, arguments, returnType);
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }
}