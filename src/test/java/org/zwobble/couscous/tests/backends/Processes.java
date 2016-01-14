package org.zwobble.couscous.tests.backends;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

public class Processes {
    public static String run(List<String> arguments, Path directoryPath) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(arguments.toArray(new String[arguments.size()]))
            .directory(directoryPath.toFile())
            .start();

        int exitCode = process.waitFor();
        String output = readString(process.getInputStream()).trim();
        String stderrOutput = readString(process.getErrorStream());
        if (exitCode != 0) {
            throw new RuntimeException("stderr was: " + stderrOutput);
        } else {
            return output;
        }
    }

    private static String readString(final InputStream stream) throws IOException {
        return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
    }
}
