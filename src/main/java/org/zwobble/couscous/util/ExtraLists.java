package org.zwobble.couscous.util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtraLists {
    @SuppressWarnings("unchecked")
    public static <T, R, E extends Exception> List<R> map(
            Stream<T> stream,
            CheckedFunction<T, R, E> function) throws E {
        try {
            return stream
                .map(value -> {
                    try {
                        return function.apply(value);
                    } catch (Exception exception) {
                        throw new StashedException(exception);
                    }
                })
                .collect(Collectors.toList());
        } catch (StashedException exception) {
            throw (E)exception.exception;
        }
    }
    
    @FunctionalInterface
    public static interface CheckedFunction<T, R, E extends Exception> {
        R apply(T value) throws E;
    }
    
    private static class StashedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        private Exception exception;
        
        private StashedException(Exception exception) {
            this.exception = exception;
        }
    }
    
    public static <T, R> R foldLeft(
            List<T> list,
            Function<T, R> initialValue,
            BiFunction<R, T, R> function) {
        R accumulator = initialValue.apply(list.get(0));
        for (int index = 1; index < list.size(); index++) {
            accumulator = function.apply(accumulator, list.get(index));
        }
        return accumulator;
    }
}
