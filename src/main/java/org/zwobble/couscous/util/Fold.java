package org.zwobble.couscous.util;

import java.util.function.BiFunction;

public class Fold {
    public static <T, V> V foldLeft(Iterable<T> iterable, V initialValue, BiFunction<V, T, V> function) {
        V value = initialValue;
        for (T element : iterable) {
            value = function.apply(value, element);
        }
        return value;
    }
}
