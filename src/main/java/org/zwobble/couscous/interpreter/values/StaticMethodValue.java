package org.zwobble.couscous.interpreter.values;

import java.util.List;
import java.util.function.BiFunction;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.Environment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class StaticMethodValue implements Callable {
    List<TypeName> argumentTypes;
    @Getter(value=AccessLevel.NONE)
    BiFunction<Environment, Arguments, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, Arguments arguments) {
        return apply.apply(environment, arguments);
    }

}
