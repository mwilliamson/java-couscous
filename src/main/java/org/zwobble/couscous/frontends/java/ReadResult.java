package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.util.TriFunction;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.zwobble.couscous.util.ExtraLists.concat;

public class ReadResult<T> {
    public static <T> ReadResult<T> of(T value) {
        return new ReadResult<>(value, Collections.emptyList());
    }

    public static <T> ReadResult<List<T>> combine(Stream<ReadResult<T>> results) {
        ImmutableList.Builder<T> values = ImmutableList.builder();
        ImmutableList.Builder<ClassNode> classes = ImmutableList.builder();

        results.forEach(result -> {
            values.add(result.getValue());
            classes.addAll(result.getClasses());
        });

        return new ReadResult<>(values.build(), classes.build());
    }

    public static <T1, T2, R> ReadResult<R> map(
        ReadResult<T1> first,
        ReadResult<T2> second,
        BiFunction<T1, T2, R> function)
    {
        R value = function.apply(first.getValue(), second.getValue());
        return new ReadResult<>(value, concat(first.getClasses(), second.getClasses()));
    }

    public static <T1, T2, T3, R> ReadResult<R> map(
        ReadResult<T1> first,
        ReadResult<T2> second,
        ReadResult<T3> third,
        TriFunction<T1, T2, T3, R> function)
    {
        R value = function.apply(first.getValue(), second.getValue(), third.getValue());
        return new ReadResult<>(value, concat(first.getClasses(), second.getClasses(), third.getClasses()));
    }

    private final T value;
    private final List<ClassNode> classes;

    public ReadResult(T value, List<ClassNode> classes) {
        this.value = value;
        this.classes = classes;
    }

    public T getValue() {
        return value;
    }

    public List<ClassNode> getClasses() {
        return classes;
    }

    public <R> ReadResult<R> map(Function<T, R> function) {
        return new ReadResult<>(function.apply(value), classes);
    }

    public <R> ReadResult<R> flatMap(Function<T, ReadResult<R>> function) {
        ReadResult<R> result = function.apply(value);
        return new ReadResult<>(result.getValue(), concat(classes, result.classes));
    }
}
