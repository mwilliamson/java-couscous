package org.zwobble.couscous.values;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.zwobble.couscous.interpreter.Arguments;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class MethodValue<T> implements Callable {
    @Getter(value=AccessLevel.NONE)
    Supplier<List<ConcreteType<?>>> argumentTypes;
    @Getter(value=AccessLevel.NONE)
    BiFunction<T, Arguments, InterpreterValue> apply;
    
    @Override
    public List<ConcreteType<?>> getArgumentTypes() {
        return argumentTypes.get();
    }
    
    public InterpreterValue apply(T receiver, Arguments arguments) {
        return apply.apply(receiver, arguments);
    }
}
