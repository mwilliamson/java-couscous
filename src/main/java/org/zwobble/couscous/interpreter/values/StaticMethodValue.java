package org.zwobble.couscous.interpreter.values;

import java.util.List;
import java.util.function.BiFunction;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.PositionalArguments;
import org.zwobble.couscous.interpreter.Environment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class StaticMethodValue implements Callable {
    List<TypeName> argumentTypes;
    @Getter(value=AccessLevel.NONE)
    BiFunction<Environment, PositionalArguments, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, PositionalArguments arguments) {
        return apply.apply(environment, arguments);
    }

}
