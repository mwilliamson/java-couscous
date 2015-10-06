package org.zwobble.couscous.interpreter.values;

import java.util.List;
import java.util.function.BiFunction;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Environment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class MethodValue implements Callable {
    List<TypeName> argumentTypes;
    @Getter(value=AccessLevel.NONE)
    BiFunction<Environment, MethodCallArguments<InterpreterValue>, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, MethodCallArguments<InterpreterValue> arguments) {
        return apply.apply(environment, arguments);
    }
}
