package org.zwobble.couscous.util;

import java.util.Iterator;

public class ExtraIterables {
    private ExtraIterables() {}

    public static <T, U> void forEach(Iterable<? extends T> first, Iterable<? extends U> second, Action2<T, U> action) {
        Iterator<? extends T> firstIterator = first.iterator();
        Iterator<? extends U> secondIterator = second.iterator();
        while (firstIterator.hasNext()) {
            action.run(firstIterator.next(), secondIterator.next());
        }
        if (secondIterator.hasNext()) {
            throw new RuntimeException("Iterables not of same length");
        }
    }
}
