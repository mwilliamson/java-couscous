package com.example;

import java.util.function.Function;

public class Lambda {
    public static int value(int factor) {
        IntFunction function = value -> value * factor * factor;
        return function.apply(21);
    }
}
