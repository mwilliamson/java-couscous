package org.zwobble.couscous.tests.backends.csharp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.csharp.CsharpBackend;
import org.zwobble.couscous.backends.python.PythonCodeGenerator;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.tests.backends.Processes;
import org.zwobble.couscous.util.ExtraLists;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class CsharpMethodRunner implements MethodRunner {
    private static final String NAMESPACE = "Couscous";

    @Override
    public PrimitiveValue runMethod(List<ClassNode> classNodes, TypeName className, String methodName, List<PrimitiveValue> arguments) {
        try {
            Path directoryPath = Files.createTempDirectory(null);
            Backend compiler = new CsharpBackend(directoryPath, NAMESPACE);
            try {
                compiler.compile(classNodes);
                return runFunction(directoryPath, className, methodName, arguments);
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (final java.lang.Throwable $ex) {
            throw new RuntimeException($ex);
        }
    }

    public static PrimitiveValue runFunction(
        Path directoryPath,
        TypeName className,
        String methodName,
        List<PrimitiveValue> arguments)
        throws IOException, InterruptedException
    {
        // TODO: use arguments
        String csharpMethodName = PythonCodeGenerator.toName(new MethodSignature(
            methodName,
            eagerMap(arguments, argument -> argument.getType())));
        String program =
            "public class MethodRunnerExample { " +
            "   public static void Main() { " +
            "       System.Console.WriteLine(" + NAMESPACE + "." + className.getQualifiedName() + "." + csharpMethodName + "());" +
            "   }" +
            "}";
        Files.write(directoryPath.resolve("MethodRunnerExample.cs"), list(program));
        Processes.run(
            ExtraLists.concat(
                list("mcs", "-out:MethodRunnerExample.exe"),
                findCsharpFiles(directoryPath)),
            directoryPath);
        String output = Processes.run(list("mono", "MethodRunnerExample.exe"), directoryPath);
        return readPrimitive(output);
    }

    private static List<String> findCsharpFiles(Path directoryPath) throws IOException {
        return ImmutableList.copyOf(
            Iterators.transform(
                Iterators.filter(
                    Files.newDirectoryStream(directoryPath).iterator(),
                    path -> path.toFile().isFile() && path.toString().endsWith(".cs")),
                path -> path.toString()));
    }

    private static PrimitiveValue readPrimitive(String output) {
        return PrimitiveValues.value(output);
    }
}
