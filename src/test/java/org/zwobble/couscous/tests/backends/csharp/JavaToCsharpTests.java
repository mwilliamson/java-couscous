package org.zwobble.couscous.tests.backends.csharp;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.couscous.CouscousCompiler;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.csharp.CsharpBackend;
import org.zwobble.couscous.frontends.java.JavaFrontend;
import org.zwobble.couscous.tests.CompilerTests;
import org.zwobble.couscous.values.PrimitiveValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaToCsharpTests extends CompilerTests {
    @Test
    @Ignore
    public void integersCanBeAssignedToObjectVariable() {
    }

    @Test
    @Ignore
    public void anonymousClassesWithoutCapture() throws Exception {
    }

    @Test
    @Ignore
    public void anonymousClassesWithCapture() throws Exception {
    }

    @Test
    @Ignore
    public void lambdasWithoutCapture() throws Exception {
    }

    @Test
    @Ignore
    public void lambdasWithCapture() throws Exception {
    }

    @Test
    @Ignore
    public void lambdasWithThisCapture() throws Exception {
    }

    @Test
    @Ignore
    public void staticMethodOverloads() throws Exception {
    }

    @Test
    @Ignore
    public void stringEquals() throws Exception {
    }

    protected PrimitiveValue execProgram(
        Path directory,
        TypeName type,
        String methodName,
        List<PrimitiveValue> arguments) throws IOException, InterruptedException
    {
        Path directoryPath = Files.createTempDirectory(null);
        try {
            CouscousCompiler compiler = new CouscousCompiler(
                new JavaFrontend(),
                new CsharpBackend(directoryPath, "Couscous"));
            compiler.compileDirectory(list(directory), directory);
            return CsharpMethodRunner.runFunction(directoryPath, type, methodName, arguments, false);
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }
}
