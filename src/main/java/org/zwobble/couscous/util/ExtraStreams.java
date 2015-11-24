package org.zwobble.couscous.util;

import java.util.Optional;
import java.util.stream.Stream;

public class ExtraStreams {
    public static <T> Stream<T> concatStreams(Stream<? extends T> first, Stream<? extends T> second, Stream<? extends T> third) {
        return Stream.concat(first, Stream.concat(second, third));
    }

    public static <T> Stream<T> toStream(Optional<T> value) {
        return value.isPresent() ? Stream.of(value.get()) : Stream.empty();
    }
}
