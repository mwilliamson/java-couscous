package org.zwobble.couscous.util;

import java.util.stream.Stream;

public class ExtraStreams {
    public static <T> Stream<T> concatStreams(Stream<? extends T> first, Stream<? extends T> second, Stream<? extends T> third) {
        return Stream.concat(first, Stream.concat(second, third));
    }
}
