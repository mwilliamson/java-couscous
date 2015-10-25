package org.zwobble.couscous.tests.backends.python;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.PythonCodeGenerator;
import org.zwobble.couscous.backends.python.PythonCompiler;
import org.zwobble.couscous.backends.python.PythonSerializer;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValues;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import static java.lang.Integer.parseInt;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public class PythonMethodRunner implements MethodRunner {
    @Override
    public PrimitiveValue runMethod(List<ClassNode> classNodes, TypeName className, String methodName, List<PrimitiveValue> arguments) {
        try {
            Path directoryPath = Files.createTempDirectory(null);
            PythonCompiler compiler = new PythonCompiler(directoryPath, "couscous");
            try {
                compiler.compile(classNodes);
                String argumentsString = Joiner.on(", ").join(arguments.stream().map(PythonCodeGenerator::generateCode).map(PythonSerializer::serialize).iterator());
                String program = "from couscous." + className.getQualifiedName() + " import " + className.getSimpleName() + ";print(repr(" + className.getSimpleName() + "." + methodName + "(" + argumentsString + ")))";
                Process process = new ProcessBuilder("python3.4", "-c", program).directory(directoryPath.toFile()).start();
                int exitCode = process.waitFor();
                String output = readString(process.getInputStream()).trim();
                String stderrOutput = readString(process.getErrorStream());
                if (exitCode != 0) {
                    throw new RuntimeException("stderr was: " + stderrOutput);
                } else {
                    return readPrimitive(output);
                }
            } finally {
                deleteRecursively(directoryPath.toFile());
            }
        } catch (final java.lang.Throwable $ex) {
            throw new RuntimeException($ex);
        }
    }
    
    private PrimitiveValue readPrimitive(String output) {
        if (output.equals("None")) {
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
    
    private String readString(final InputStream stream) throws IOException {
        return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
    }
}