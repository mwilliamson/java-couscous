package org.zwobble.couscous.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ExtraLists {
    public static <T> List<T> list() {
        return ImmutableList.of();
    }

    public static <T> List<T> list(T value1) {
        return ImmutableList.of(value1);
    }

    public static <T> List<T> list(T value1, T value2) {
        return ImmutableList.of(value1, value2);
    }

    public static <T> List<T> list(T value1, T value2, T value3) {
        return ImmutableList.of(value1, value2, value3);
    }

    public static <T> List<T> list(T... values) {
        return ImmutableList.copyOf(values);
    }

    public static <T> List<T> cons(T value, Iterable<T> list) {
        return concat(list(value), list);
    }

    public static <T> List<T> append(Iterable<? extends T> iterable, T value) {
        return Stream.concat(ExtraIterables.stream(iterable), Stream.of(value)).collect(Collectors.toList());
    }

    public static <T> List<T> concat(Iterable<? extends T> first, Iterable<? extends T> second) {
        return ImmutableList.copyOf(Iterables.concat(first, second));
    }

    public static <T> List<T> concat(List<? extends T> first, List<? extends T> second, List<? extends T> third) {
        return Stream.concat(Stream.concat(first.stream(), second.stream()), third.stream()).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends T> List<R> ofType(List<T> list, Class<R> clazz) {
        return (List<R>)list.stream()
            .filter(clazz::isInstance)
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T, R, E extends Exception> List<R> flatMap(
            Stream<T> stream,
            CheckedFunction<T, Iterable<R>, E> function) throws E {
        try {
            return stream
                .flatMap(value -> {
                    try {
                        return StreamSupport.stream(function.apply(value).spliterator(), false);
                    } catch (Exception exception) {
                        throw new StashedException(exception);
                    }
                })
                .collect(Collectors.toList());
        } catch (StashedException exception) {
            throw (E)exception.exception;
        }
    }

    public static <T> List<T> eagerFilter(Iterable<T> iterable, Predicate<T> predicate) {
        return StreamSupport.stream(iterable.spliterator(), false)
            .filter(predicate)
            .collect(Collectors.toList());
    }

    public static <T, R> List<R> eagerMap(
            Iterable<T> iterable,
            Function<T, R> function) {
        return StreamSupport.stream(iterable.spliterator(), false)
            .map(function)
            .collect(Collectors.toList());
    }

    public static <T, R> List<R> eagerMapWithIndex(
        Iterable<T> iterable,
        FunctionWithIndex<T, R> function)
    {
        ImmutableList.Builder<R> builder = ImmutableList.builder();
        int index = 0;
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            builder.add(function.apply(iterator.next(), index));
            index++;
        }
        return builder.build();
    }

    public interface FunctionWithIndex<T, R> {
        R apply(T value, int index);
    }

    public static <T1, T2, R> List<R> eagerMap(
        Iterable<T1> iterable1,
        Iterable<T2> iterable2,
        BiFunction<T1, T2, R> function)
    {
        Iterator<T1> iterator1 = iterable1.iterator();
        Iterator<T2> iterator2 = iterable2.iterator();

        ImmutableList.Builder<R> result = ImmutableList.builder();

        while (iterator1.hasNext() && iterator2.hasNext()) {
            result.add(function.apply(iterator1.next(), iterator2.next()));
        }

        if (iterator1.hasNext()) {
            throw new RuntimeException("iterable1 longer than iterable2");
        }

        if (iterator2.hasNext()) {
            throw new RuntimeException("iterable2 longer than iterable1");
        }

        return result.build();
    }

    public static <T, R> List<R> eagerFlatMap(
        Iterable<T> iterable,
        Function<T, Iterable<R>> function) {
        return StreamSupport.stream(iterable.spliterator(), false)
            .flatMap(element -> StreamSupport.stream(function.apply(element).spliterator(), false))
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

    public static <T> List<T> copyOf(Iterable<T> iterable) {
        return ImmutableList.copyOf(iterable);
    }
}
