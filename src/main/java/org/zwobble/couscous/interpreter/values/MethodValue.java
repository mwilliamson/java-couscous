package org.zwobble.couscous.interpreter.values;

import java.util.List;
import java.util.function.BiFunction;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Arguments;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class MethodValue<T> implements Callable {
    List<TypeName> argumentTypes;
    @Getter(value=AccessLevel.NONE)
    BiFunction<T, Arguments, InterpreterValue> apply;
    
    public InterpreterValue apply(T receiver, Arguments arguments) {
        return apply.apply(receiver, arguments);
    }
}
