package org.zwobble.couscous.tests.backends.python;

import com.google.common.base.Joiner;
import org.hamcrest.Matchers;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.ast.types.Type;
import org.zwobble.couscous.backends.Names;
import org.zwobble.couscous.backends.python.PythonBackend;
import org.zwobble.couscous.backends.python.PythonCodeGenerator;
import org.zwobble.couscous.backends.python.PythonSerializer;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.tests.backends.Processes;
import org.zwobble.couscous.values.PrimitiveValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.couscous.tests.util.ExtraFiles.deleteRecursively;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.values.PrimitiveValues.UNIT;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public class PythonMethodRunner implements MethodRunner {
    @Override
    public PrimitiveValue runMethod(List<TypeNode> classNodes, ScalarType className, String methodName, List<PrimitiveValue> arguments, Type returnType) {
        try {
            Path directoryPath = Files.createTempDirectory(null);
            PythonBackend compiler = new PythonBackend(directoryPath, "couscous");
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
            throws IOException, InterruptedException {
        
        String argumentsString = Joiner.on(", ").join(arguments.stream().map(PythonCodeGenerator::generateCode).map(PythonSerializer::serialize).iterator());
        MethodSignature signature = new MethodSignature(
            methodName,
            eagerMap(arguments, argument -> argument.getType()),
            returnType);
        String pythonMethodName = Names.toUniqueName(signature);
        String program = "from couscous." + className.getQualifiedName() + " import " + className.getSimpleName() + ";" +
            "value = " + className.getSimpleName() + "." + pythonMethodName + "(" + argumentsString + ");" +
            "print(type(value)); print(value)";
        String output = Processes.run(list("python3.4", "-c", program), directoryPath);
        return readPrimitive(output);
    }

    private static final String PACKAGE_PREFIX = "couscous.";

    private static PrimitiveValue readPrimitive(String output) {
        String[] lines = output.split("[\\r\\n]+");
        String type = lines[0];
        String value = lines[1];
        switch (type) {
            case "<class 'int'>":
                return value(parseInt(value));
            case "<class 'str'>":
                return value(value);
            case "<class 'bool'>":
                return value(value.equals("True"));
            case "<class 'NoneType'>":
                return UNIT;
            case "<class 'type'>":
                String typeName = value.substring(value.indexOf("'") + 1, value.lastIndexOf("'"));
                assertThat(typeName, Matchers.startsWith(PACKAGE_PREFIX));
                return value(ScalarType.of(typeName.substring(PACKAGE_PREFIX.length(), typeName.lastIndexOf("."))));
            default:
                throw new RuntimeException("Unhandled type: " + type);
        }
    }
}