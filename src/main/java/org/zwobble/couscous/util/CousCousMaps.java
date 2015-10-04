package org.zwobble.couscous.util;

import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class CousCousMaps {
    public static <K1, K2, V> Map<K2, V> mapKeys(Map<K1, V> map, Function<K1, K2> func) {
        return map.entrySet()
            .stream()
            .collect(toMap(entry -> func.apply(entry.getKey()), entry -> entry.getValue()));
    }
}
