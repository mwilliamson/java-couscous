package org.zwobble.couscous.values;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.zwobble.couscous.interpreter.Arguments;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class StaticMethodValue implements Callable {
    @Getter(value=AccessLevel.NONE)
    Supplier<List<ConcreteType<?>>> argumentTypes;
    @Getter(value=AccessLevel.NONE)
    Function<Arguments, InterpreterValue> apply;
    
    @Override
    public List<ConcreteType<?>> getArgumentTypes() {
        return argumentTypes.get();
    }
    
    public InterpreterValue apply(Arguments arguments) {
        return apply.apply(arguments);
    }

}
