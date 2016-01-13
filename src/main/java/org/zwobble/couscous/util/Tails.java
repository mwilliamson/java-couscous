package org.zwobble.couscous.util;

import java.util.Iterator;
import java.util.List;

public class Tails {
    public static <T> Iterable<List<T>> tails(List<T> list) {
        return () -> new Iterator<List<T>>() {
            private int startIndex = 0;

            @Override
            public boolean hasNext() {
                return startIndex < list.size();
            }

            @Override
            public List<T> next() {
                return list.subList(startIndex++, list.size());
            }
        };
    }

    public static <T> List<T> tail(List<T> list) {
        return list.subList(1, list.size());
    }
}
