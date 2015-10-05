package org.zwobble.couscous.tests.backends.python;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.backends.python.PythonCompiler;
import org.zwobble.couscous.tests.BackendTests;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.values.InterpreterValue;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.values.InterpreterValues.UNIT;
import static org.zwobble.couscous.values.InterpreterValues.value;

import lombok.SneakyThrows;
import lombok.val;

public class PythonCompilerTests extends BackendTests {
    @Override
    protected MethodRunner buildMethodRunner() {
        return new MethodRunner() {
            @Override
            @SneakyThrows
            public InterpreterValue runMethod(
                    ClassNode classNode,
                    String methodName,
                    List<InterpreterValue> arguments) {
                val compiler = new PythonCompiler();
                val directoryPath = Files.createTempDirectory(null);
                try {
                    compiler.compile(asList(classNode), directoryPath);

                    val program = "from " + classNode.getName() + " import " + classNode.getLocalName() +
                        ";print(repr(" + classNode.getLocalName() + "." + methodName + "()))";
                    
                    val process = new ProcessBuilder("python3.4", "-c", program)
                        .directory(directoryPath.toFile())
                        .start();
                    val exitCode = process.waitFor();
                    val output = readString(process.getInputStream()).trim();
                    val stderrOutput = readString(process.getErrorStream());
                    if (exitCode != 0) {
                        throw new RuntimeException("stderr was: " + stderrOutput);
                    } else if (output.equals("None")) {
                        return UNIT;
                    } else if (output.startsWith("'")) {
                        return value(output.substring(1, output.length() - 1));
                    } else {
                        return value(parseInt(output));
                    }
                } finally {
                    deleteRecursively(directoryPath.toFile());
                }
            }

            private String readString(final InputStream stream) throws IOException {
                return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
            }
        };
    }
}
