package com.example;

import java.util.function.Function;

public class Lambda {
    public static int value(int factor) {
        Function<Integer, Integer> function = value -> value * factor;
        return function.apply(21);
    }
}
