package org.zwobble.couscous.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    
    public static <T, R> List<R> eagerMap(
            Iterable<T> iterable,
            Function<T, R> function) {
        return StreamSupport.stream(iterable.spliterator(), false)
            .map(function)
            .collect(Collectors.toList());
    }
    
    @FunctionalInterface
    public interface CheckedFunction<T, R, E extends Exception> {
        R apply(T value) throws E;
    }
    
    private static class StashedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        private Exception exception;
        
        private StashedException(Exception exception) {
            this.exception = exception;
        }
    }
}
