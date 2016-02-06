package org.zwobble.couscous.tests.backends.csharp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.hamcrest.Matchers;
import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.csharp.CsharpBackend;
import org.zwobble.couscous.backends.csharp.CsharpSerializer;
import org.zwobble.couscous.backends.python.PythonCodeGenerator;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.tests.backends.Processes;
import org.zwobble.couscous.util.ExtraLists;
import org.zwobble.couscous.values.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
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
                ClassNode classNode = Iterables.find(classNodes, c -> c.getName().equals(className));
                boolean isVoid = Iterables.find(classNode.getMethods(), method -> method.getName().equals(methodName))
                    .getReturnType()
                    .equals(UnitValue.REF);
                return runFunction(directoryPath, className, methodName, arguments, isVoid);
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (final java.lang.Throwable $ex) {
            throw new RuntimeException($ex);
        }
    }

    private static PrimitiveValue runFunction(
        Path directoryPath,
        TypeName className,
        String methodName,
        List<PrimitiveValue> arguments,
        boolean isVoid)
        throws IOException, InterruptedException
    {
        // TODO: use arguments
        String csharpMethodName = PythonCodeGenerator.toName(new MethodSignature(
            methodName,
            eagerMap(arguments, argument -> argument.getType())));
        String value = CsharpSerializer.serialize(
            staticMethodCall(
                TypeName.of(NAMESPACE + "." + className.getQualifiedName()),
                csharpMethodName,
                eagerMap(arguments, LiteralNode::literal),
                // TODO: pass in return type
                ObjectValues.OBJECT));

        String body = isVoid
            ? value + ";"
            : "var value = " + value + ";" +
                "       System.Console.WriteLine(value.GetType());" +
                "       System.Console.WriteLine(value);";

        String program =
            "public class MethodRunnerExample { " +
            "   public static void Main() { " +
            "       " + body +
            "   }" +
            "}";
        Files.write(directoryPath.resolve("MethodRunnerExample.cs"), list(program));
        Processes.run(
            ExtraLists.concat(
                list("mcs", "-out:MethodRunnerExample.exe"),
                findCsharpFiles(directoryPath)),
            directoryPath);
        String output = Processes.run(list("mono", "MethodRunnerExample.exe"), directoryPath);
        if (isVoid) {
            return PrimitiveValues.UNIT;
        } else {
            return readPrimitive(output);
        }
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
