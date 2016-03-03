package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.PositionalArguments;

import java.util.List;
import java.util.function.BiFunction;

public final class StaticMethodValue implements Callable {
    private final List<TypeName> argumentTypes;
    private final BiFunction<Environment, PositionalArguments, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, PositionalArguments arguments) {
        return apply.apply(environment, arguments);
    }
    
    public StaticMethodValue(final List<TypeName> argumentTypes, final BiFunction<Environment, PositionalArguments, InterpreterValue> apply) {
        this.argumentTypes = argumentTypes;
        this.apply = apply;
    }
    
    public List<TypeName> getArgumentTypes() {
        return this.argumentTypes;
    }
}