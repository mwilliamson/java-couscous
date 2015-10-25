package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.PositionalArguments;

public class MethodCallArguments<T> {
    private final T receiver;
    private final PositionalArguments positionalArguments;
    
    public InterpreterValue get(int index) {
        return positionalArguments.get(index);
    }
    
    private MethodCallArguments(final T receiver, final PositionalArguments positionalArguments) {
        this.receiver = receiver;
        this.positionalArguments = positionalArguments;
    }
    
    public static <T> MethodCallArguments<T> of(final T receiver, final PositionalArguments positionalArguments) {
        return new MethodCallArguments<T>(receiver, positionalArguments);
    }
    
    public T getReceiver() {
        return this.receiver;
    }
    
    public PositionalArguments getPositionalArguments() {
        return this.positionalArguments;
    }
}