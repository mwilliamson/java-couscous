package org.zwobble.couscous.interpreter;

import java.util.List;
import java.util.Optional;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import org.zwobble.couscous.interpreter.values.InterpreterValue;

import com.google.common.collect.ImmutableMap;

public class Interpreter {
    private Project project;
    
    public Interpreter(Project project) {
        this.project = project;
    }
    
    public InterpreterValue run(TypeName className, String methodName, List<InterpreterValue> arguments) {
        final Environment environment = new Environment(
            project,
            Optional.empty(),
            ImmutableMap.of());
        
        ConcreteType clazz = environment.findClass(className);
        return clazz.callStaticMethod(environment, methodName, arguments);
    }
}