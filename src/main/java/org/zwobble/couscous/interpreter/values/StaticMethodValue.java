package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.types.Type;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.PositionalArguments;

import java.util.List;
import java.util.function.BiFunction;

public final class StaticMethodValue implements Callable {
    private final List<Type> argumentTypes;
    private final BiFunction<Environment, PositionalArguments, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, PositionalArguments arguments) {
        return apply.apply(environment, arguments);
    }
    
    public StaticMethodValue(final List<Type> argumentTypes, final BiFunction<Environment, PositionalArguments, InterpreterValue> apply) {
        this.argumentTypes = argumentTypes;
        this.apply = apply;
    }
    
    public List<Type> getArgumentTypes() {
        return this.argumentTypes;
    }
}