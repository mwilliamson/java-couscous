package org.zwobble.couscous.tests.backends.csharp;

import org.zwobble.couscous.CouscousCompiler;
import org.zwobble.couscous.frontends.java.JavaFrontend;
import org.zwobble.couscous.tests.CompilerTests;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.values.PrimitiveValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaToCsharpTests extends CompilerTests {
    protected PrimitiveValue execProgram(
        Path directory,
        ScalarType type,
        String methodName,
        List<PrimitiveValue> arguments,
        ScalarType returnType) throws IOException, InterruptedException
    {
        Path directoryPath = Files.createTempDirectory(null);
        try {
            CouscousCompiler compiler = new CouscousCompiler(
                new JavaFrontend(),
                new CsharpTestBackend(directoryPath, CsharpMethodRunner.NAMESPACE));
            compiler.compileDirectory(list(directory), directory);
            return CsharpMethodRunner.runFunction(directoryPath, type, methodName, arguments, returnType);
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }
}
