package org.zwobble.couscous.util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ExtraLists {
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
