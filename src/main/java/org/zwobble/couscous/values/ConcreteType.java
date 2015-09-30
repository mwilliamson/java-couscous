package org.zwobble.couscous.values;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.NoSuchMethod;

import com.google.common.collect.ImmutableMap;

import lombok.val;

public class ConcreteType<T> {
    public static class Builder<T> {
        private final ImmutableMap.Builder<String, BiFunction<T, Arguments, InterpreterValue>> methods =
            ImmutableMap.builder();
        
        public Builder<T> method(String name, BiFunction<T, Arguments, InterpreterValue> method) {
            methods.put(name, method);
            return this;
        }
        
        public ConcreteType<T> build() {
            return new ConcreteType<T>(methods.build());
        }
    }

    private Map<String, BiFunction<T, Arguments, InterpreterValue>> methods;

    public ConcreteType(Map<String, BiFunction<T, Arguments, InterpreterValue>> methods) {
        this.methods = methods;
    }

    public static <T> ConcreteType.Builder<T> builder() {
        return new Builder<>();
    }

    public InterpreterValue callMethod(InterpreterValue receiver, String methodName, List<InterpreterValue> arguments) {
        if (!methods.containsKey(methodName)) {
            throw new NoSuchMethod(methodName);
        }
        val method = methods.get(methodName);
        return method.apply((T)receiver, new Arguments(arguments));
    }
}
