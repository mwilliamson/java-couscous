package org.zwobble.couscous.tests.util.processes;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

public class Processes {
    public static ExecutionResult run(List<String> arguments) throws IOException, InterruptedException {
        return run(arguments, null);
    }

    public static ExecutionResult run(List<String> arguments, Path directoryPath) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(arguments.toArray(new String[arguments.size()]))
            .directory(directoryPath == null ? null : directoryPath.toFile())
            .start();

        int exitCode = process.waitFor();
        String output = readString(process.getInputStream()).trim();
        String stderrOutput = readString(process.getErrorStream());
        return new ExecutionResult(exitCode, output, stderrOutput);
    }

    private static String readString(final InputStream stream) throws IOException {
        return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
    }
}
