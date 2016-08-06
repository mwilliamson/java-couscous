package org.zwobble.couscous.tests.backends.csharp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import org.hamcrest.Matchers;
import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.backends.csharp.CsharpCodeGenerator;
import org.zwobble.couscous.backends.csharp.CsharpSerializer;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.tests.backends.Processes;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.util.ExtraLists;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public class CsharpMethodRunner implements MethodRunner {
    static final String NAMESPACE = "ExampleProject.Couscous";

    @Override
    public PrimitiveValue runMethod(List<TypeNode> classNodes, ScalarType className, String methodName, List<PrimitiveValue> arguments, Type returnType) {
        try {
            Path directoryPath = Files.createTempDirectory(null);
            Backend compiler = new CsharpTestBackend(directoryPath, NAMESPACE);
            try {
                compiler.compile(classNodes);
                return runFunction(directoryPath, className, methodName, arguments, returnType);
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (final java.lang.Throwable $ex) {
            throw new RuntimeException($ex);
        }
    }

    public static PrimitiveValue runFunction(
        Path directoryPath,
        ScalarType className,
        String methodName,
        List<PrimitiveValue> arguments,
        Type returnType)
        throws IOException, InterruptedException
    {
        MethodSignature signature = new MethodSignature(
            methodName,
            list(),
            eagerMap(arguments, argument -> argument.getType()),
            returnType);
        String csharpMethodName = CsharpCodeGenerator.NAMING.methodName(signature);
        String value = CsharpSerializer.serialize(
            staticMethodCall(
                ScalarType.of(NAMESPACE + "." + className.getQualifiedName()),
                csharpMethodName,
                eagerMap(arguments, LiteralNode::literal),
                returnType));

        boolean isVoid = returnType.equals(Types.VOID);
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
        String executablePath = compileFiles(
            findCsharpFiles(directoryPath),
            directoryPath
        );
        String output = Processes.run(list("mono", executablePath));
        if (isVoid) {
            return PrimitiveValues.UNIT;
        } else {
            return readPrimitive(output);
        }
    }

    private static String compileFiles(List<String> paths, Path directoryPath) throws IOException, InterruptedException {
        // TODO: find cache directory properly
        Path cacheDirectory = Paths.get(System.getProperty("user.home"), ".cache/couscous/tests/csharp");
        String hash = hashFiles(paths);
        Path cacheExecutable = cacheDirectory.resolve(hash);
        if (!cacheExecutable.toFile().exists()) {
            cacheDirectory.toFile().mkdirs();
            Processes.run(
                ExtraLists.concat(
                    list("mcs", "-out:" + cacheExecutable.toString()),
                    paths
                ),
                directoryPath
            );
        }
        return cacheExecutable.toString();
    }

    private static String hashFiles(List<String> paths) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            for (String path : Ordering.natural().immutableSortedCopy(paths)) {
                byte[] bytes = Files.readAllBytes(Paths.get(path));
                digest.update(bytes);
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private static List<String> findCsharpFiles(Path directoryPath) throws IOException {
        return ImmutableList.copyOf(
            Iterators.transform(
                Iterators.filter(
                    Files.newDirectoryStream(directoryPath).iterator(),
                    path -> path.toFile().isFile() && path.toString().endsWith(".cs")),
                path -> path.toString()));
    }

    private static final String NAMESPACE_PREFIX = NAMESPACE + ".";

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
                    return value(Types.STRING);
                } else {
                    assertThat(value, Matchers.startsWith(NAMESPACE_PREFIX));
                    return value(ScalarType.of(value.substring(NAMESPACE_PREFIX.length())));
                }
            default:
                throw new RuntimeException("Unhandled type: " + type);
        }
    }
}
