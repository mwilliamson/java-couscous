package org.zwobble.couscous.util;

import java.util.Iterator;

public class NaturalNumbers implements Iterable<Integer> {
    public static final Iterable<Integer> INSTANCE = new NaturalNumbers();

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int next = 0;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                return next++;
            }
        };
    }
}
