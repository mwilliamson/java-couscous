package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.Arguments;

import java.util.List;
import java.util.function.BiFunction;

public final class StaticMethodValue implements Callable {
    private final List<Type> argumentTypes;
    private final BiFunction<Environment, Arguments, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, Arguments arguments) {
        return apply.apply(environment, arguments);
    }
    
    public StaticMethodValue(final List<Type> argumentTypes, final BiFunction<Environment, Arguments, InterpreterValue> apply) {
        this.argumentTypes = argumentTypes;
        this.apply = apply;
    }
    
    public List<Type> getArgumentTypes() {
        return this.argumentTypes;
    }
}