package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.interpreter.Arguments;

public class MethodCallArguments<T> {
    private final T receiver;
    private final Arguments arguments;
    
    public InterpreterValue get(int index) {
        return arguments.get(index);
    }
    
    private MethodCallArguments(final T receiver, final Arguments arguments) {
        this.receiver = receiver;
        this.arguments = arguments;
    }
    
    public static <T> MethodCallArguments<T> of(final T receiver, final Arguments arguments) {
        return new MethodCallArguments<T>(receiver, arguments);
    }
    
    public T getReceiver() {
        return this.receiver;
    }
    
    public Arguments getArguments() {
        return this.arguments;
    }
}