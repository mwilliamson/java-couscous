package org.zwobble.couscous.tests.backends.python;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.PythonBackend;
import org.zwobble.couscous.backends.python.PythonCodeGenerator;
import org.zwobble.couscous.backends.python.PythonSerializer;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;
import org.zwobble.couscous.values.StringValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public class PythonMethodRunner implements MethodRunner {
    @Override
    public PrimitiveValue runMethod(List<ClassNode> classNodes, TypeName className, String methodName, List<PrimitiveValue> arguments) {
        try {
            Path directoryPath = Files.createTempDirectory(null);
            PythonBackend compiler = new PythonBackend(directoryPath, "couscous");
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
            throws IOException, InterruptedException {
        
        String argumentsString = Joiner.on(", ").join(arguments.stream().map(PythonCodeGenerator::generateCode).map(PythonSerializer::serialize).iterator());
        String pythonMethodName = PythonCodeGenerator.toName(new MethodSignature(
            methodName,
            eagerMap(arguments, argument -> argument.getType())));
        String program = "from couscous." + className.getQualifiedName() + " import " + className.getSimpleName() + ";" +
            "print(repr(" + className.getSimpleName() + "." + pythonMethodName + "(" + argumentsString + ")))";
        Process process = new ProcessBuilder("python3.4", "-c", program).directory(directoryPath.toFile()).start();
        int exitCode = process.waitFor();
        String output = readString(process.getInputStream()).trim();
        String stderrOutput = readString(process.getErrorStream());
        if (exitCode != 0) {
            throw new RuntimeException("stderr was: " + stderrOutput);
        } else {
            return readPrimitive(output);
        }
    }

    private static final Pattern TYPE_REGEX = Pattern.compile("^<class '([^']+)'>$");

    private static PrimitiveValue readPrimitive(String output) {
        Matcher matcher = TYPE_REGEX.matcher(output);
        if (matcher.matches()) {
            String name = matcher.group(1);
            if (name.equals("couscous.java.lang.String.String")) {
                return value(StringValue.REF);
            } else {
                throw new UnsupportedOperationException();
            }
        } else if (output.equals("None")) {
            return PrimitiveValues.UNIT;
        } else if (output.startsWith("\'")) {
            return value(output.substring(1, output.length() - 1));
        } else if (output.equals("True")) {
            return value(true);
        } else if (output.equals("False")) {
            return value(false);
        } else {
            return value(parseInt(output));
        }
    }
    
    private static String readString(final InputStream stream) throws IOException {
        return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
    }
}