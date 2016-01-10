package org.zwobble.couscous.cli;

import org.apache.commons.cli.*;
import org.zwobble.couscous.Backend;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.backends.python.PythonBackend;
import org.zwobble.couscous.frontends.java.JavaFrontend;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.util.ExtraLists.eagerFlatMap;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class CouscousCli {
    public static final String SOURCEPATH = "sourcepath";
    public static final String BACKEND = "backend";
    public static final String OUTPUT = "output";

    public static void main(String[] rawArguments) throws Exception {
        CommandLine arguments = parseArguments(rawArguments);

        JavaFrontend frontend = new JavaFrontend();
        List<ClassNode> classNodes = eagerFlatMap(
            arguments.getArgList(),
            path -> {
                try {
                    return frontend.readSourceDirectory(
                        eagerMap(sourcePath(arguments), sourcePath -> path(sourcePath)),
                        path(path));
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });

        Backend backend = backend(
            arguments.getOptionValue(BACKEND),
            arguments.getOptionValue(OUTPUT));
        backend.compile(classNodes);
    }

    private static List<String> sourcePath(CommandLine arguments) {
        if (arguments.hasOption(SOURCEPATH)) {
            return asList(arguments.getOptionValues(SOURCEPATH));
        } else {
            return list();
        }
    }

    private static Backend backend(String backend, String output) {
        switch (backend) {
            case "python":
                return new PythonBackend(path(output), "_couscous");
            default:
                throw new RuntimeException("Unrecognised backend: " + backend);
        }
    }

    private static Path path(String output) {
        return FileSystems.getDefault().getPath(output);
    }

    private static CommandLine parseArguments(String[] rawArguments) throws ParseException {
        Options options = new Options();
        options.addOption(requiredOption(BACKEND));
        options.addOption(requiredOption(OUTPUT));
        options.addOption(multipleOption(SOURCEPATH));

        return new DefaultParser().parse(options, rawArguments);
    }

    private static Option requiredOption(String name) {
        return Option.builder()
            .longOpt(name)
            .required()
            .hasArg()
            .build();
    }

    private static Option multipleOption(String name) {
        return Option.builder()
            .longOpt(name)
            .hasArgs()
            .build();
    }
}
