package org.zwobble.couscous.tests;

import com.google.common.collect.ImmutableList;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.frontends.java.JavaFrontend;
import org.zwobble.couscous.interpreter.Interpreter;
import org.zwobble.couscous.interpreter.JavaProject;
import org.zwobble.couscous.interpreter.Project;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.InterpreterValues;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.util.FileSet;
import org.zwobble.couscous.values.PrimitiveValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaToInterpreterTests extends CompilerTests {
    @Override
    protected PrimitiveValue execProgram(
        Path directory,
        ScalarType type,
        String methodName,
        List<PrimitiveValue> arguments,
        ScalarType returnType
    ) throws IOException, InterruptedException {
        JavaFrontend frontend = new JavaFrontend();
        List<TypeNode> classNodes = frontend.readSourceDirectory(list(directory), FileSet.directory(directory));

        Project project = JavaProject.of(classNodes);
        Interpreter interpreter = new Interpreter(project);
        List<InterpreterValue> argumentValues = ImmutableList.copyOf(transform(arguments, InterpreterValues::value));

        return interpreter.run(type, methodName, argumentValues, returnType)
            .toPrimitiveValue().get();
    }
}
