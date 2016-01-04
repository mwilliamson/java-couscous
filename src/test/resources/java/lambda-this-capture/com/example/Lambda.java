package com.example;

import java.util.function.Function;

public class Lambda {
    public static int value(int factor) {
        return new Lambda(factor).run();
    }
    
    private final int factor;
    
    private Lambda(int factor) {
        this.factor = factor;
    }
    
    private int run() {
        Function<Integer, Integer> function = value -> value * factor;
        return function.apply(21);
    }
}
