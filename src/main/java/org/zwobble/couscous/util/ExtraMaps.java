package org.zwobble.couscous.util;

import com.google.common.collect.ImmutableMap;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ExtraMaps {
    public static <K, V> Map<K, V> map() {
        return ImmutableMap.of();
    }

    public static <K, V> Map<K, V> map(K key1, V value1) {
        return ImmutableMap.of(key1, value1);
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2) {
        return ImmutableMap.of(key1, value1, key2, value2);
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2, K key3, V value3) {
        return ImmutableMap.of(key1, value1, key2, value2, key3, value3);
    }

    public static <K, V> Optional<V> lookup(Map<K, V> map, K key) {
        return Optional.ofNullable(map.get(key));
    }

    public static <T, K, V> Map<K, V> toMap(Iterable<K> keys, Iterable<V> values) {
        return toMap(ExtraIterables.map(keys, values, ExtraMaps::entry));
    }

    public static <T, K, V> Map<K, V> toMap(Iterable<Map.Entry<K, V>> entries) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        entries.forEach(builder::put);
        return builder.build();
    }

    public static <T, K, V> Map<K, V> toMap(Iterable<T> iterable, Function<T, Map.Entry<K, V>> function) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();

        for (T value : iterable) {
            builder.put(function.apply(value));
        }

        return builder.build();
    }

    public static <T, K> Map<K, T> toMapWithKeys(Iterable<T> iterable, Function<T, K> function) {
        return toMap(iterable, value -> entry(function.apply(value), value));
    }

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}
