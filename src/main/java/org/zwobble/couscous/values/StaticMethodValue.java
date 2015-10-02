package org.zwobble.couscous.values;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.Environment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class StaticMethodValue implements Callable {
    @Getter(value=AccessLevel.NONE)
    Supplier<List<ConcreteType<?>>> argumentTypes;
    @Getter(value=AccessLevel.NONE)
    BiFunction<Environment, Arguments, InterpreterValue> apply;
    
    @Override
    public List<ConcreteType<?>> getArgumentTypes() {
        return argumentTypes.get();
    }
    
    public InterpreterValue apply(Environment environment, Arguments arguments) {
        return apply.apply(environment, arguments);
    }

}
