package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.PositionalArguments;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName="of")
@Getter
public class MethodCallArguments<T> {
    private final T receiver;
    private final PositionalArguments positionalArguments;
    
    public InterpreterValue get(int index) {
        return positionalArguments.get(index);
    }
}
