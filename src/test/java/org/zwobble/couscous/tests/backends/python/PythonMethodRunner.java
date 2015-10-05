package org.zwobble.couscous.tests.backends.python;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
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
import static java.util.Arrays.asList;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.values.PrimitiveValues.value;

import lombok.SneakyThrows;
import lombok.val;

public class PythonMethodRunner implements MethodRunner {
    @Override
    @SneakyThrows
    public PrimitiveValue runMethod(
            ClassNode classNode,
            String methodName,
            List<PrimitiveValue> arguments) {
        val directoryPath = Files.createTempDirectory(null);
        val compiler = new PythonCompiler(directoryPath);
        try {
            compiler.compile(asList(classNode));
            
            val argumentsString = Joiner.on(", ").join(arguments.stream()
                .map(PythonCodeGenerator::generateCode)
                .map(PythonSerializer::serialize)
                .iterator());
            
            val program = "from " + classNode.getName() + " import " + classNode.getLocalName() +
                ";print(repr(" + classNode.getLocalName() + "." + methodName + "(" + argumentsString + ")))";
            
            val process = new ProcessBuilder("python3.4", "-c", program)
                .directory(directoryPath.toFile())
                .start();
            val exitCode = process.waitFor();
            val output = readString(process.getInputStream()).trim();
            val stderrOutput = readString(process.getErrorStream());
            if (exitCode != 0) {
                throw new RuntimeException("stderr was: " + stderrOutput);
            } else {
                return readPrimitive(output);
            }
        } finally {
            deleteRecursively(directoryPath.toFile());
        }
    }

    private PrimitiveValue readPrimitive(String output) {
        if (output.equals("None")) {
            return PrimitiveValues.UNIT;
        } else if (output.startsWith("'")) {
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
