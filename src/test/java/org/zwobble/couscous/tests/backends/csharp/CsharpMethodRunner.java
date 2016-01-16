package org.zwobble.couscous.tests.backends.csharp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.hamcrest.Matchers;
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
import org.zwobble.couscous.values.StringValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.values.PrimitiveValues.value;

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
            "       var value = " + NAMESPACE + "." + className.getQualifiedName() + "." + csharpMethodName + "();" +
            "       System.Console.WriteLine(value.GetType());" +
            "       System.Console.WriteLine(value);" +
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

    private static final String NAMESPACE_PREFIX = "Couscous.";

    private static PrimitiveValue readPrimitive(String output) {
        String[] lines = output.split("[\\r\\n]+");
        String type = lines[0];
        String value = lines[1];
        switch (type) {
            case "System.Int32":
                return value(parseInt(value));
            case "System.String":
                return value(value);
            case "System.Boolean":
                return value(value.equals("True"));
            case "System.MonoType":
                if (value.equals("System.String")) {
                    return value(StringValue.REF);
                } else {
                    assertThat(value, Matchers.startsWith(NAMESPACE_PREFIX));
                    return value(TypeName.of(value.substring(NAMESPACE_PREFIX.length())));
                }
            default:
                throw new RuntimeException("Unhandled type: " + type);
        }
    }
}
