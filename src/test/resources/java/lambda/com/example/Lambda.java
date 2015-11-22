package com.example;

import java.util.function.Function;

public class Lambda {
    public static int value() {
        Function<Integer, Integer> function = value -> value * 2;
        return function.apply(21);
    }
}
