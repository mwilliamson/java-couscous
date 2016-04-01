package org.zwobble.couscous.util;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class ExtraSets {
    private ExtraSets() {}

    public static <T> Set<T> set() {
        return ImmutableSet.of();
    }

    public static <T> Set<T> set(T value1) {
        return ImmutableSet.of(value1);
    }

    public static <T> Set<T> set(T... values) {
        return ImmutableSet.copyOf(values);
    }
}
