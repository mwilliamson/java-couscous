package org.zwobble.couscous.util;

import com.google.common.collect.Iterables;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.zwobble.couscous.util.ExtraLists.list;

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

    public static <T1, T2, R> Iterable<R> map(Iterable<T1> iterable1, Iterable<T2> iterable2, BiFunction<T1, T2, R> function) {
        return new Iterable<R>() {
            @Override
            public Iterator<R> iterator() {
                Iterator<T1> iterator1 = iterable1.iterator();
                Iterator<T2> iterator2 = iterable2.iterator();
                return map(iterator1, iterator2, function);
            }
        };
    }

    private static <R, T1, T2> Iterator<R> map(Iterator<T1> iterator1, Iterator<T2> iterator2, BiFunction<T1, T2, R> function) {
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return iterator1.hasNext() && iterator2.hasNext();
            }

            @Override
            public R next() {
                return function.apply(iterator1.next(), iterator2.next());
            }
        };
    }

    public static <T> Iterable<T> cast(Class<T> type, Iterable<?> iterable) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return cast(type, iterable.iterator());
            }
        };
    }

    private static <T> Iterator<T> cast(Class<T> type, Iterator<?> iterator) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                Object next = iterator.next();
                if (type.isInstance(next)) {
                    return (T) next;
                } else {
                    throw new ClassCastException();
                }
            }
        };
    }

    public static <T> Iterable<T> lazyAppend(Iterable<T> head, T tail) {
        return Iterables.concat(head, list(tail));
    }

    public static <T> Iterable<T> lazyCons(T head, Iterable<T> tails) {
        return Iterables.concat(list(head), tails);
    }

    public static <T, R> Iterable<R> lazyMap(Iterable<T> iterable, Function<T, R> function) {
        return new Iterable<R>() {
            @Override
            public Iterator<R> iterator() {
                return map(iterable.iterator(), function);
            }
        };
    }

    private static <T, R> Iterator<R> map(Iterator<T> iterator, Function<T, R> function) {
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R next() {
                return function.apply(iterator.next());
            }
        };
    }

    public static <T, R> Iterable<R> lazyFlatMap(Iterable<T> iterable, Function<T, Iterable<R>> function) {
        return iterable(() -> stream(iterable).flatMap(element -> stream(function.apply(element))));
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T, E> Iterable<T> iterable(Supplier<Stream<T>> stream) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return stream.get().iterator();
            }
        };
    }
}
