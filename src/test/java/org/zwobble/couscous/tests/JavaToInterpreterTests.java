package org.zwobble.couscous.tests;

import com.google.common.collect.ImmutableList;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.frontends.java.JavaFrontend;
import org.zwobble.couscous.interpreter.Interpreter;
import org.zwobble.couscous.interpreter.JavaProject;
import org.zwobble.couscous.interpreter.Project;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.InterpreterValues;
import org.zwobble.couscous.values.PrimitiveValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

public class JavaToInterpreterTests extends CompilerTests {
    @Override
    protected PrimitiveValue execProgram(
        Path directory,
        TypeName type,
        String methodName,
        List<PrimitiveValue> arguments
    ) throws IOException, InterruptedException {
        JavaFrontend frontend = new JavaFrontend();
        List<ClassNode> classNodes = frontend.readSourceDirectory(directory, directory);

        Project project = JavaProject.of(classNodes);
        Interpreter interpreter = new Interpreter(project);
        List<InterpreterValue> argumentValues = ImmutableList.copyOf(transform(arguments, InterpreterValues::value));

        return interpreter.run(type, methodName, argumentValues)
            .toPrimitiveValue().get();
    }
}
