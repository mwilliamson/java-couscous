package com.example;

import java.util.function.Supplier;

public class AnonymousClass {
    public static int value(int x) {
        Supplier<Integer> supplier = new Supplier<Integer>() {
            @Override
            public Integer get() {
                return x;
            }
        };
        
        return supplier.get();
    }
}
