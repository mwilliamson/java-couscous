package org.zwobble.couscous.util;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ExtraArrays {
    public static <T, R> Stream<R> mapWithIndex(T[] array, BiFunction<Integer, T, R> function) {
        return IntStream.range(0, array.length)
            .mapToObj(index -> function.apply(index,  array[index]));
    }
    
    public static <T, R> Stream<R> map(T[] array, Function<T, R> function) {
        return stream(array).map(function);
    }
    
    public static <T> Stream<T> stream(T[] array) {
        return StreamSupport.stream(Arrays.spliterator(array), false);
    }
}
