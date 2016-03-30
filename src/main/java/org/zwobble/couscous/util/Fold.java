package org.zwobble.couscous.util;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class Fold {
    public static <T, V> V foldLeft(Iterable<T> iterable, V initialValue, BiFunction<V, T, V> function) {
        V value = initialValue;
        for (T element : iterable) {
            value = function.apply(value, element);
        }
        return value;
    }
    public static <T> T foldLeft(Iterable<T> iterable, BiFunction<T, T, T> function) {
        Iterator<T> iterator = iterable.iterator();
        T value = iterator.next();
        while (iterator.hasNext()) {
            value = function.apply(value, iterator.next());
        }
        return value;
    }

    public static <T, V> V foldRight(List<T> iterable, V initialValue, BiFunction<V, T, V> function) {
        return foldLeft(Lists.reverse(iterable), initialValue, function);
    }
}
