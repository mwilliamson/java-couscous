package org.zwobble.couscous.tests;

import org.zwobble.couscous.CouscousCompiler;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.PythonBackend;
import org.zwobble.couscous.frontends.java.JavaFrontend;
import org.zwobble.couscous.tests.backends.python.PythonMethodRunner;
import org.zwobble.couscous.values.PrimitiveValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public class JavaToPythonTests extends CompilerTests {
    protected PrimitiveValue execProgram(
            Path directory,
            TypeName type,
            String methodName,
            List<PrimitiveValue> arguments) throws IOException, InterruptedException {

        Path directoryPath = Files.createTempDirectory(null);
        try {
            CouscousCompiler compiler = new CouscousCompiler(
                new JavaFrontend(),
                new PythonBackend(directoryPath, "couscous"));
            compiler.compileDirectory(directory, directory);
            return PythonMethodRunner.runFunction(directoryPath, type, methodName, arguments);
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }
}