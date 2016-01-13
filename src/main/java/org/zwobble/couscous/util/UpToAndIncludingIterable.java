package org.zwobble.couscous.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class UpToAndIncludingIterable {
    public static <T> Iterable<T> upToAndIncluding(Iterable<T> iterable, Predicate<T> predicate) {
        return () -> new UpToIterator<>(iterable.iterator(), predicate);
    }

    private static class UpToIterator<T> implements Iterator<T> {
        private final Iterator<T> iterator;
        private final Predicate<T> predicate;
        private boolean finished;

        public UpToIterator(Iterator<T> iterator, Predicate<T> predicate) {
            this.iterator = iterator;
            this.predicate = predicate;
        }

        @Override
        public boolean hasNext() {
            return !finished && iterator.hasNext();
        }

        @Override
        public T next() {
            if (hasNext()) {
                T value = iterator.next();
                finished = predicate.test(value);
                return value;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
