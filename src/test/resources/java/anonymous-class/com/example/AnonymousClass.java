package com.example;

import java.util.function.Supplier;

public class AnonymousClass {
    public static int value() {
        Supplier<Integer> supplier = new Supplier<Integer>() {
            @Override
            public Integer get() {
                return 42;
            }
        };
        
        return supplier.get();
    }
}
