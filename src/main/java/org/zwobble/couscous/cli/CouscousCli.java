package org.zwobble.couscous.cli;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.apache.commons.cli.*;
import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.backends.csharp.CsharpBackend;
import org.zwobble.couscous.backends.python.PythonBackend;
import org.zwobble.couscous.frontends.java.JavaFrontend;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.util.ExtraLists.*;

public class CouscousCli {
    public static final String FILES = "files";
    public static final String SOURCEPATH = "sourcepath";
    public static final String BACKEND = "backend";
    public static final String OUTPUT = "output";

    public static void main(String[] rawArguments) throws Exception {
        JsonObject configuration = Json.parse(new FileReader("couscous.json")).asObject();
        String backendName = configuration.get(BACKEND).asString();
        String outputDirectory = configuration.get(OUTPUT).asString();
        List<String> sourcePaths = eagerMap(
            configuration.get(SOURCEPATH).asArray().values(),
            JsonValue::asString);
        List<String> files = eagerMap(
            configuration.get(FILES).asArray().values(),
            JsonValue::asString);

        Backend backend = backend(backendName, outputDirectory);

        JavaFrontend frontend = new JavaFrontend();
        try {
            List<TypeNode> classNodes = frontend.readSourceDirectory(
                paths(sourcePaths),
                paths(files));
            backend.compile(classNodes);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Backend backend(String backend, String output) {
        switch (backend) {
            case "python":
                return new PythonBackend(path(output), "_couscous");
            case "csharp":
                return new CsharpBackend(path(output), "_Couscous");
            default:
                throw new RuntimeException("Unrecognised backend: " + backend);
        }
    }

    private static List<Path> paths(List<String> paths) {
        return eagerMap(paths, path -> path(path));
    }

    private static Path path(String output) {
        return FileSystems.getDefault().getPath(output);
    }
}
