package org.zwobble.couscous.interpreter;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.StaticReceiverValue;

import java.util.List;
import java.util.Optional;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class Interpreter {
    private Project project;
    
    public Interpreter(Project project) {
        this.project = project;
    }
    
    public InterpreterValue run(TypeName className, String methodName, List<InterpreterValue> arguments, TypeName returnType) {
        final Environment environment = new Environment(
            project,
            Optional.empty(),
            ImmutableMap.of());
        
        StaticReceiverValue clazz = environment.findClass(className);
        MethodSignature signature = new MethodSignature(
            methodName,
            eagerMap(arguments, argument -> argument.getType().getName()),
            returnType);
        return clazz.callMethod(environment, signature, arguments);
    }
}