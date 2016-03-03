package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Environment;

import java.util.List;
import java.util.function.BiFunction;

public final class MethodValue implements Callable {
    private final List<TypeName> argumentTypes;
    private final BiFunction<Environment, MethodCallArguments<InterpreterValue>, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, MethodCallArguments<InterpreterValue> arguments) {
        return apply.apply(environment, arguments);
    }
    
    public MethodValue(final List<TypeName> argumentTypes, final BiFunction<Environment, MethodCallArguments<InterpreterValue>, InterpreterValue> apply) {
        this.argumentTypes = argumentTypes;
        this.apply = apply;
    }
    
    public List<TypeName> getArgumentTypes() {
        return this.argumentTypes;
    }
}