package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.interpreter.Environment;

import java.util.List;
import java.util.function.BiFunction;

public final class MethodValue implements Callable {
    private final List<Type> argumentTypes;
    private final BiFunction<Environment, MethodCallArguments<InterpreterValue>, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, MethodCallArguments<InterpreterValue> arguments) {
        return apply.apply(environment, arguments);
    }
    
    public MethodValue(final List<Type> argumentTypes, final BiFunction<Environment, MethodCallArguments<InterpreterValue>, InterpreterValue> apply) {
        this.argumentTypes = argumentTypes;
        this.apply = apply;
    }
    
    public List<Type> getArgumentTypes() {
        return this.argumentTypes;
    }
}