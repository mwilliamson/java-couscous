package org.zwobble.couscous.values;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.interpreter.NoSuchMethod;
import org.zwobble.couscous.interpreter.UnexpectedValueType;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;

import com.google.common.collect.ImmutableMap;

import lombok.val;

public class ConcreteType<T> {
    public static class Builder<T> {
        private final ImmutableMap.Builder<String, MethodValue<T>> methods =
            ImmutableMap.builder();
        
        public Builder<T> method(
                String name,
                List<ConcreteType<?>> argumentsTypes,
                BiFunction<T, Arguments, InterpreterValue> method) {
            methods.put(name, new MethodValue<T>(argumentsTypes, method));
            return this;
        }
        
        public ConcreteType<T> build() {
            return new ConcreteType<T>(methods.build());
        }
    }

    private Map<String, MethodValue<T>> methods;

    public ConcreteType(Map<String, MethodValue<T>> methods) {
        this.methods = methods;
    }

    public static <T> ConcreteType.Builder<T> builder() {
        return new Builder<>();
    }

    @SuppressWarnings("unchecked")
    public InterpreterValue callMethod(InterpreterValue receiver, String methodName, List<InterpreterValue> arguments) {
        if (!methods.containsKey(methodName)) {
            throw new NoSuchMethod(methodName);
        }
        val method = methods.get(methodName);
        if (method.getArgumentTypes().size() != arguments.size()) {
            throw new WrongNumberOfArguments(method.getArgumentTypes().size(), arguments.size());
        }
        
        for (int index = 0; index < arguments.size(); index++) {
            val formalArgumentType = method.getArgumentTypes().get(index);
            val actualArgumentType = arguments.get(index).getType();
            if (formalArgumentType != actualArgumentType) {
                throw new UnexpectedValueType(formalArgumentType, actualArgumentType);
            }
        }
        
        return method.apply((T)receiver, new Arguments(arguments));
    }
}
