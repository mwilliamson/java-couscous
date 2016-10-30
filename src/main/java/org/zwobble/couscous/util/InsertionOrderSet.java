package org.zwobble.couscous.util;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface InsertionOrderSet<T> extends Iterable<T> {
    static <T> InsertionOrderSet<T> copyOf(Iterable<T> iterable) {
        List<T> list = StreamSupport.stream(iterable.spliterator(), false)
            .distinct()
            .collect(Collectors.toList());
        return new InsertionOrderSet<T>() {
            @Override
            public List<T> asList() {
                return list;
            }

            @Override
            public Iterator<T> iterator() {
                return list.iterator();
            }

            @Override
            public boolean isEmpty() {
                return list.isEmpty();
            }
        };
    }

    List<T> asList();
    boolean isEmpty();
}
